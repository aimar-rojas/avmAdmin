package aimar.rojas.avmadmin.core.sync

import aimar.rojas.avmadmin.core.data.local.AvmDatabase
import aimar.rojas.avmadmin.data.local.SessionDataStore
import aimar.rojas.avmadmin.features.parties.data.CreatePartyRequest
import aimar.rojas.avmadmin.features.parties.data.PartiesApiService
import aimar.rojas.avmadmin.features.parties.data.PartyDto
import aimar.rojas.avmadmin.features.parties.data.local.PartyDao
import aimar.rojas.avmadmin.features.parties.data.local.entities.PartyEntity
import aimar.rojas.avmadmin.features.selections.data.SelectionByTradeDto
import aimar.rojas.avmadmin.features.selections.data.SelectionsApiService
import aimar.rojas.avmadmin.features.selections.data.UpdateSelectionRequestDto
import aimar.rojas.avmadmin.features.selections.data.local.SelectionDao
import aimar.rojas.avmadmin.features.selections.data.local.entities.SelectionEntity
import aimar.rojas.avmadmin.features.selections.data.local.entities.SelectionWithUnitWeights
import aimar.rojas.avmadmin.features.selections.data.local.entities.UnitWeightEntity
import aimar.rojas.avmadmin.features.shipments.data.CreateShipmentRequest
import aimar.rojas.avmadmin.features.shipments.data.ShipmentDto
import aimar.rojas.avmadmin.features.shipments.data.ShipmentsApiService
import aimar.rojas.avmadmin.features.shipments.data.local.ShipmentDao
import aimar.rojas.avmadmin.features.shipments.data.local.entities.ShipmentEntity
import aimar.rojas.avmadmin.features.trades.data.CreateTradeRequest
import aimar.rojas.avmadmin.features.trades.data.TradeDto
import aimar.rojas.avmadmin.features.trades.data.TradesApiService
import aimar.rojas.avmadmin.features.trades.data.UpdateTradeRequest
import aimar.rojas.avmadmin.features.trades.data.local.TradeDao
import aimar.rojas.avmadmin.features.trades.data.local.entities.TradeEntity
import aimar.rojas.avmadmin.utils.DateUtils
import androidx.room.withTransaction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.sync.withPermit
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ManualSyncManager @Inject constructor(
    private val database: AvmDatabase,
    private val sessionDataStore: SessionDataStore,
    private val partyDao: PartyDao,
    private val shipmentDao: ShipmentDao,
    private val tradeDao: TradeDao,
    private val selectionDao: SelectionDao,
    private val partiesApiService: PartiesApiService,
    private val shipmentsApiService: ShipmentsApiService,
    private val tradesApiService: TradesApiService,
    private val selectionsApiService: SelectionsApiService
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val syncMutex = Mutex()
    private val _status = MutableStateFlow(SyncStatus())
    val status: StateFlow<SyncStatus> = _status

    private val syncTimestampFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    init {
        scope.launch {
            _status.update {
                it.copy(
                    lastAttemptAt = sessionDataStore.getLastManualSyncAttempt(),
                    lastSuccessAt = sessionDataStore.getLastManualSyncSuccess()
                )
            }
        }

        scope.launch {
            combine(
                partyDao.observePendingCount(),
                shipmentDao.observePendingCount(),
                tradeDao.observePendingCount(),
                selectionDao.observePendingCount()
            ) { partyCount, shipmentCount, tradeCount, selectionCount ->
                SyncEntitySummary(
                    partyPending = partyCount,
                    shipmentPending = shipmentCount,
                    tradePending = tradeCount,
                    selectionPending = selectionCount
                )
            }.collect { summary ->
                _status.update { current -> current.copy(summary = summary) }
            }
        }
    }

    suspend fun syncNow(): SyncStatus = syncMutex.withLock {
        if (_status.value.isRunning) {
            return _status.value
        }

        val startedAt = nowIso()
        sessionDataStore.saveLastManualSyncAttempt(startedAt)
        _status.update {
            it.copy(
                state = "syncing",
                phase = "Preparando sincronización",
                message = null,
                isRunning = true,
                result = SyncResultSummary(),
                lastAttemptAt = startedAt
            )
        }

        var resultSummary = SyncResultSummary()
        return try {
            resultSummary = resultSummary.merge(syncParties())
            resultSummary = resultSummary.merge(syncShipments())
            resultSummary = resultSummary.merge(syncTrades())
            resultSummary = resultSummary.merge(syncSelections())

            pullParties()
            pullShipments()
            pullTrades()
            pullSelections()

            val finishedAt = nowIso()
            sessionDataStore.saveLastManualSyncSuccess(finishedAt)
            _status.update {
                it.copy(
                    state = if (resultSummary.failedItems > 0) "partial_failure" else "success",
                    phase = null,
                    result = resultSummary,
                    message = if (resultSummary.failedItems > 0) "Sincronización completada con fallos parciales." else "Sincronización completada.",
                    lastSuccessAt = finishedAt,
                    isRunning = false
                )
            }
            _status.value
        } catch (e: Exception) {
            _status.update {
                it.copy(
                    state = "error",
                    phase = null,
                    result = resultSummary,
                    message = e.message ?: "Error de sincronización manual",
                    isRunning = false
                )
            }
            _status.value
        }
    }

    private suspend fun syncParties(): SyncResultSummary {
        _status.update { it.copy(phase = "Subiendo contactos") }
        val pending = partyDao.getPendingSyncParties()
        var successCount = 0
        var failedCount = 0
        syncConcurrent(pending, 4) { entity ->
            val now = nowIso()
            val syncingEntity = entity.copy(syncState = SyncState.SYNCING, lastSyncAttemptAt = now, syncError = null)
            partyDao.insertParty(syncingEntity)
            if (entity.remoteId == null) {
                val response = partiesApiService.createParty(
                    CreatePartyRequest(
                        partyRole = entity.partyRole,
                        aliasName = entity.aliasName,
                        firstName = entity.firstName,
                        lastName = entity.lastName,
                        dni = entity.dni,
                        ruc = entity.ruc,
                        phone = entity.phone,
                        accountNumber = entity.accountNumber
                    )
                )
                if (response.isSuccessful && response.body() != null) {
                    val dto = response.body()!!.party
                    val updated = syncingEntity.mergeRemote(dto, now)
                    partyDao.insertParty(updated)
                    successCount++
                } else {
                    partyDao.insertParty(syncingEntity.copy(syncState = SyncState.failureStateFor(entity.syncState), syncError = response.errorBody()?.string()))
                    failedCount++
                }
            } else {
                val response = partiesApiService.updateParty(
                    entity.remoteId,
                    aimar.rojas.avmadmin.features.parties.data.UpdatePartyRequest(
                        partyRole = entity.partyRole,
                        aliasName = entity.aliasName,
                        firstName = entity.firstName,
                        lastName = entity.lastName,
                        dni = entity.dni,
                        ruc = entity.ruc,
                        phone = entity.phone,
                        accountNumber = entity.accountNumber
                    )
                )
                if (response.isSuccessful && response.body() != null) {
                    val dto = response.body()!!.party
                    val updated = syncingEntity.mergeRemote(dto, now)
                    partyDao.insertParty(updated)
                    successCount++
                } else {
                    partyDao.insertParty(syncingEntity.copy(syncState = SyncState.failureStateFor(entity.syncState), syncError = response.errorBody()?.string()))
                    failedCount++
                }
            }
        }
        return SyncResultSummary(pushedParties = successCount, failedItems = failedCount)
    }

    private suspend fun syncShipments(): SyncResultSummary {
        _status.update { it.copy(phase = "Subiendo envíos") }
        val pending = shipmentDao.getPendingSyncShipments()
        var successCount = 0
        var failedCount = 0
        syncConcurrent(pending, 4) { entity ->
            val now = nowIso()
            val syncingEntity = entity.copy(syncState = SyncState.SYNCING, lastSyncAttemptAt = now, syncError = null)
            shipmentDao.insertShipment(syncingEntity)
            if (entity.remoteId == null) {
                val response = shipmentsApiService.createShipment(
                    CreateShipmentRequest(
                        startDate = DateUtils.formatToApiDate(entity.startDate),
                        endDate = entity.endDate?.let { DateUtils.formatToApiDate(it) },
                        status = entity.status
                    )
                )
                if (response.isSuccessful && response.body() != null) {
                    val dto = response.body()!!.shipment
                    val updated = syncingEntity.mergeRemote(dto, now)
                    shipmentDao.insertShipment(updated)
                    successCount++
                } else {
                    shipmentDao.insertShipment(syncingEntity.copy(syncState = SyncState.failureStateFor(entity.syncState), syncError = response.errorBody()?.string()))
                    failedCount++
                }
            } else {
                val response = shipmentsApiService.updateShipment(
                    entity.remoteId,
                    aimar.rojas.avmadmin.features.shipments.data.UpdateShipmentRequest(
                        startDate = DateUtils.formatToApiDate(entity.startDate),
                        endDate = entity.endDate?.let { DateUtils.formatToApiDate(it) },
                        status = entity.status
                    )
                )
                if (response.isSuccessful && response.body() != null) {
                    val dto = response.body()!!.shipment
                    val updated = syncingEntity.mergeRemote(dto, now)
                    shipmentDao.insertShipment(updated)
                    successCount++
                } else {
                    shipmentDao.insertShipment(syncingEntity.copy(syncState = SyncState.failureStateFor(entity.syncState), syncError = response.errorBody()?.string()))
                    failedCount++
                }
            }
        }
        return SyncResultSummary(pushedShipments = successCount, failedItems = failedCount)
    }

    private suspend fun syncTrades(): SyncResultSummary {
        _status.update { it.copy(phase = "Subiendo negocios") }
        val pending = tradeDao.getPendingSyncTrades()
        var successCount = 0
        var failedCount = 0
        syncConcurrent(pending, 4) { entity ->
            val now = nowIso()
            val syncingEntity = entity.copy(syncState = SyncState.SYNCING, lastSyncAttemptAt = now, syncError = null)
            tradeDao.insertTrade(syncingEntity)

            val partyRemoteId = partyDao.getPartyById(entity.partyLocalId)?.remoteId
            val shipmentRemoteId = shipmentDao.getShipmentById(entity.shipmentLocalId)?.remoteId
            if (partyRemoteId == null || shipmentRemoteId == null) {
                tradeDao.insertTrade(syncingEntity.copy(syncState = SyncState.failureStateFor(entity.syncState), syncError = "Dependencias sin remoteId"))
                failedCount++
                return@syncConcurrent
            }

            val response = if (entity.remoteId == null) {
                tradesApiService.createTrade(
                    CreateTradeRequest(
                        partyId = partyRemoteId,
                        shipmentId = shipmentRemoteId,
                        tradeType = entity.tradeType,
                        startDatetime = entity.startDatetime,
                        endDatetime = entity.endDatetime,
                        discountWeightPerTray = entity.discountWeightPerTray,
                        varietyAvocado = entity.varietyAvocado
                    )
                )
            } else {
                tradesApiService.updateTrade(
                    entity.remoteId,
                    UpdateTradeRequest(
                        partyId = partyRemoteId,
                        shipmentId = shipmentRemoteId,
                        tradeType = entity.tradeType,
                        startDatetime = entity.startDatetime,
                        endDatetime = entity.endDatetime,
                        discountWeightPerTray = entity.discountWeightPerTray,
                        varietyAvocado = entity.varietyAvocado
                    )
                )
            }

            if (response.isSuccessful && response.body() != null) {
                val dto = response.body()!!.trade
                val updated = syncingEntity.mergeRemote(dto, now)
                tradeDao.insertTrade(updated)
                successCount++
            } else {
                tradeDao.insertTrade(syncingEntity.copy(syncState = SyncState.failureStateFor(entity.syncState), syncError = response.errorBody()?.string()))
                failedCount++
            }
        }
        return SyncResultSummary(pushedTrades = successCount, failedItems = failedCount)
    }

    private suspend fun syncSelections(): SyncResultSummary {
        _status.update { it.copy(phase = "Subiendo selecciones") }
        val pending = selectionDao.getPendingSelections()
        var successCount = 0
        var failedCount = 0
        syncConcurrent(pending, 6) { selectionWithWeights ->
            val entity = selectionWithWeights.selection
            val now = nowIso()
            val syncingSelection = entity.copy(syncState = SyncState.SYNCING, lastSyncAttemptAt = now, syncError = null)
            selectionDao.insertSelection(syncingSelection)

            val tradeRemoteId = tradeDao.getTradeById(entity.tradeLocalId)?.remoteId
            if (tradeRemoteId == null) {
                selectionDao.insertSelection(syncingSelection.copy(syncState = SyncState.failureStateFor(entity.syncState), syncError = "Negocio sin remoteId"))
                failedCount++
                return@syncConcurrent
            }

            val request = UpdateSelectionRequestDto(
                tradeId = tradeRemoteId,
                selectionTypeId = entity.selectionTypeId,
                price = entity.price?.toString(),
                unitWeights = selectionWithWeights.unitWeights.map {
                    aimar.rojas.avmadmin.features.selections.data.UnitWeightRequestDto(
                        weight = it.weight.toString(),
                        amount = it.amount
                    )
                }
            )

            val response = if (entity.remoteId == null) {
                selectionsApiService.createSelection(request)
            } else {
                selectionsApiService.updateSelection(entity.remoteId, request)
            }

            if (response.isSuccessful && response.body() != null) {
                database.withTransaction {
                    val remoteSelection = response.body()!!.selection
                    upsertRemoteSelection(remoteSelection, now)
                }
                successCount++
            } else {
                selectionDao.insertSelection(syncingSelection.copy(syncState = SyncState.failureStateFor(entity.syncState), syncError = response.errorBody()?.string()))
                failedCount++
            }
        }
        return SyncResultSummary(pushedSelections = successCount, failedItems = failedCount)
    }

    private suspend fun pullParties() {
        _status.update { it.copy(phase = "Actualizando contactos") }
        val response = partiesApiService.getParties(updatedAfter = sessionDataStore.getLastPartySync())
        if (response.isSuccessful && response.body() != null) {
            val now = nowIso()
            response.body()!!.parties.forEach { dto -> upsertRemoteParty(dto, now) }
            sessionDataStore.saveLastPartySync(now)
        }
    }

    private suspend fun pullShipments() {
        _status.update { it.copy(phase = "Actualizando envíos") }
        val response = shipmentsApiService.getShipments(updatedAfter = sessionDataStore.getLastShipmentSync())
        if (response.isSuccessful && response.body() != null) {
            val now = nowIso()
            response.body()!!.shipments.forEach { dto -> upsertRemoteShipment(dto, now) }
            sessionDataStore.saveLastShipmentSync(now)
        }
    }

    private suspend fun pullTrades() {
        _status.update { it.copy(phase = "Actualizando negocios") }
        val lastSync = sessionDataStore.getLastTradeSync() ?: "2000-01-01T00:00:00Z"
        val response = tradesApiService.getTrades(shipmentId = null, updatedAfter = lastSync)
        if (response.isSuccessful && response.body() != null) {
            val now = nowIso()
            response.body()!!.trades.forEach { dto -> upsertRemoteTrade(dto, now) }
            sessionDataStore.saveLastTradeSync(now)
        }
    }

    private suspend fun pullSelections() {
        _status.update { it.copy(phase = "Actualizando selecciones") }
        val response = selectionsApiService.getSelections()
        if (response.isSuccessful && response.body() != null) {
            val now = nowIso()
            database.withTransaction {
                response.body()!!.selections.forEach { dto -> upsertRemoteSelection(dto, now) }
            }
            sessionDataStore.saveLastSelectionSync(now)
        }
    }

    private suspend fun upsertRemoteParty(dto: PartyDto, now: String) {
        val existing = partyDao.getPartyByRemoteId(dto.partyId)
        if (existing != null && SyncState.isPending(existing.syncState)) return
        partyDao.insertParty(
            PartyEntity(
                localId = existing?.localId ?: dto.partyId,
                remoteId = dto.partyId,
                partyRole = dto.partyRole,
                aliasName = dto.aliasName,
                firstName = dto.firstName ?: "",
                lastName = dto.lastName,
                dni = dto.dni,
                ruc = dto.ruc,
                phone = dto.phone,
                accountNumber = dto.accountNumber,
                syncState = SyncState.CLEAN,
                lastSyncedAt = now,
                serverUpdatedAt = dto.updatedAt
            )
        )
    }

    private suspend fun upsertRemoteShipment(dto: ShipmentDto, now: String) {
        val existing = shipmentDao.getShipmentByRemoteId(dto.shipmentId)
        if (existing != null && SyncState.isPending(existing.syncState)) return
        shipmentDao.insertShipment(
            ShipmentEntity(
                localId = existing?.localId ?: dto.shipmentId,
                remoteId = dto.shipmentId,
                startDate = DateUtils.parseApiDate(dto.startDate) ?: Date(),
                endDate = DateUtils.parseApiDate(dto.endDate),
                status = dto.status,
                amountPerShipment = existing?.amountPerShipment ?: 0.0,
                syncState = SyncState.CLEAN,
                lastSyncedAt = now,
                serverUpdatedAt = dto.updatedAt
            )
        )
    }

    private suspend fun upsertRemoteTrade(dto: TradeDto, now: String) {
        dto.party?.let { upsertRemoteParty(it, now) }
        dto.shipment?.let { upsertRemoteShipment(it, now) }
        
        val partyLocalId = partyDao.getPartyByRemoteId(dto.partyId)?.localId ?: return
        val shipmentLocalId = shipmentDao.getShipmentByRemoteId(dto.shipmentId)?.localId ?: return
        val existing = tradeDao.getTradeByRemoteId(dto.tradeId)
        if (existing != null && SyncState.isPending(existing.syncState)) return
        tradeDao.insertTrade(
            TradeEntity(
                localId = existing?.localId ?: dto.tradeId,
                remoteId = dto.tradeId,
                partyLocalId = partyLocalId,
                bossId = dto.bossId,
                shipmentLocalId = shipmentLocalId,
                tradeType = dto.tradeType,
                startDatetime = dto.startDatetime,
                endDatetime = dto.endDatetime,
                discountWeightPerTray = dto.discountWeightPerTray,
                varietyAvocado = dto.varietyAvocado ?: "Hass",
                amountPerTrade = dto.amountPerTrade,
                syncState = SyncState.CLEAN,
                lastSyncedAt = now,
                serverUpdatedAt = dto.updatedAt
            )
        )
    }

    private suspend fun upsertRemoteSelection(dto: SelectionByTradeDto, now: String) {
        val tradeLocalId = tradeDao.getTradeByRemoteId(dto.tradeId)?.localId ?: return
        val existing = selectionDao.getSelectionWithUnitWeightsByRemoteId(dto.selectionByTradeId)
        if (existing != null && SyncState.isPending(existing.selection.syncState)) return

        val selectionEntity = SelectionEntity(
            localId = existing?.selection?.localId ?: dto.selectionByTradeId,
            remoteId = dto.selectionByTradeId,
            tradeLocalId = tradeLocalId,
            selectionTypeId = dto.selectionTypeId,
            price = dto.price?.toDoubleOrNull(),
            selectionTypeName = dto.selectionType?.nameSelection,
            syncState = SyncState.CLEAN,
            lastSyncedAt = now,
            serverUpdatedAt = dto.updatedAt
        )
        val selectionLocalId = selectionDao.insertSelection(selectionEntity).toInt()
        selectionDao.deleteUnitWeightsBySelectionId(selectionLocalId)
        selectionDao.insertUnitWeights(
            dto.unitWeights.orEmpty().map { unitWeight ->
                UnitWeightEntity(
                    localId = unitWeight.unitWeightId,
                    remoteId = unitWeight.unitWeightId,
                    selectionLocalId = selectionLocalId,
                    weight = unitWeight.weight.toDoubleOrNull() ?: 0.0,
                    amount = unitWeight.amount
                )
            }
        )
    }

    private suspend fun PartyEntity.mergeRemote(dto: PartyDto, now: String): PartyEntity {
        return copy(
            remoteId = dto.partyId,
            partyRole = dto.partyRole,
            aliasName = dto.aliasName,
            firstName = dto.firstName ?: "",
            lastName = dto.lastName,
            dni = dto.dni,
            ruc = dto.ruc,
            phone = dto.phone,
            accountNumber = dto.accountNumber,
            syncState = SyncState.CLEAN,
            lastSyncedAt = now,
            serverUpdatedAt = dto.updatedAt,
            syncError = null
        )
    }

    private suspend fun ShipmentEntity.mergeRemote(dto: ShipmentDto, now: String): ShipmentEntity {
        return copy(
            remoteId = dto.shipmentId,
            startDate = DateUtils.parseApiDate(dto.startDate) ?: startDate,
            endDate = DateUtils.parseApiDate(dto.endDate),
            status = dto.status,
            syncState = SyncState.CLEAN,
            lastSyncedAt = now,
            serverUpdatedAt = dto.updatedAt,
            syncError = null
        )
    }

    private suspend fun TradeEntity.mergeRemote(dto: TradeDto, now: String): TradeEntity {
        val partyLocalId = partyDao.getPartyByRemoteId(dto.partyId)?.localId ?: partyLocalId
        val shipmentLocalId = shipmentDao.getShipmentByRemoteId(dto.shipmentId)?.localId ?: shipmentLocalId
        return copy(
            remoteId = dto.tradeId,
            partyLocalId = partyLocalId,
            shipmentLocalId = shipmentLocalId,
            bossId = dto.bossId,
            tradeType = dto.tradeType,
            startDatetime = dto.startDatetime,
            endDatetime = dto.endDatetime,
            discountWeightPerTray = dto.discountWeightPerTray,
            varietyAvocado = dto.varietyAvocado ?: "Hass",
            amountPerTrade = dto.amountPerTrade,
            syncState = SyncState.CLEAN,
            lastSyncedAt = now,
            serverUpdatedAt = dto.updatedAt,
            syncError = null
        )
    }

    private suspend fun <T> syncConcurrent(
        items: List<T>,
        concurrency: Int,
        block: suspend (T) -> Unit
    ) {
        val semaphore = Semaphore(concurrency)
        kotlinx.coroutines.coroutineScope {
            items.map { item ->
                async {
                    semaphore.withPermit {
                        block(item)
                    }
                }
            }.awaitAll()
        }
    }

    private fun SyncResultSummary.merge(other: SyncResultSummary): SyncResultSummary {
        return SyncResultSummary(
            pushedParties = pushedParties + other.pushedParties,
            pushedShipments = pushedShipments + other.pushedShipments,
            pushedTrades = pushedTrades + other.pushedTrades,
            pushedSelections = pushedSelections + other.pushedSelections,
            failedItems = failedItems + other.failedItems
        )
    }

    private fun nowIso(): String = syncTimestampFormat.format(Date())
}
