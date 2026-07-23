package aimar.rojas.avmadmin.core.sync

import aimar.rojas.avmadmin.core.data.local.AvmDatabase
import aimar.rojas.avmadmin.data.local.SessionDataStore
import aimar.rojas.avmadmin.features.apuntes.data.ApuntesApiService
import aimar.rojas.avmadmin.features.apuntes.data.CreateApunteDetailRequestDto
import aimar.rojas.avmadmin.features.apuntes.data.CreateApunteRequestDto
import aimar.rojas.avmadmin.features.apuntes.data.local.ApuntesDao
import aimar.rojas.avmadmin.features.apuntes.data.local.entities.ApunteWithDetails
import aimar.rojas.avmadmin.features.apuntes.data.toEntity
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
import android.util.Log
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
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.atomic.AtomicInteger
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
    private val apuntesDao: ApuntesDao,
    private val partiesApiService: PartiesApiService,
    private val shipmentsApiService: ShipmentsApiService,
    private val tradesApiService: TradesApiService,
    private val selectionsApiService: SelectionsApiService,
    private val apuntesApiService: ApuntesApiService
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
                selectionDao.observePendingCount(),
                apuntesDao.observePendingCount()
            ) { partyCount, shipmentCount, tradeCount, selectionCount, apunteCount ->
                SyncEntitySummary(
                    partyPending = partyCount,
                    shipmentPending = shipmentCount,
                    tradePending = tradeCount,
                    selectionPending = selectionCount,
                    apuntePending = apunteCount
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
        Log.i(TAG, "Manual sync started at $startedAt")
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
        val pullErrors = mutableListOf<String>()
        return try {
            pullParties()?.let { pullErrors.add(it) }
            pullShipments()?.let { pullErrors.add(it) }
            pullTrades()?.let { pullErrors.add(it) }
            pullSelections()?.let { pullErrors.add(it) }
            pullApuntes()?.let { pullErrors.add(it) }

            resultSummary = resultSummary.merge(syncParties())
            resultSummary = resultSummary.merge(syncShipments())
            resultSummary = resultSummary.merge(syncTrades())
            resultSummary = resultSummary.merge(syncSelections())
            resultSummary = resultSummary.merge(syncApuntes())

            if (pullErrors.isNotEmpty()) {
                resultSummary = resultSummary.copy(
                    failedItems = resultSummary.failedItems + pullErrors.size
                )
            }

            val finishedAt = nowIso()
            val remainingPending = getTotalPendingCount()
            val completedSuccessfully = resultSummary.failedItems == 0 && remainingPending == 0
            Log.i(
                TAG,
                "Manual sync finished. success=$completedSuccessfully, failed=${resultSummary.failedItems}, remaining=$remainingPending, pullErrors=${pullErrors.size}"
            )
            if (completedSuccessfully) {
                sessionDataStore.saveLastManualSyncSuccess(finishedAt)
            }
            _status.update {
                it.copy(
                    state = if (completedSuccessfully) "success" else "partial_failure",
                    phase = null,
                    result = resultSummary,
                    message = buildSyncMessage(completedSuccessfully, pullErrors, remainingPending),
                    lastSuccessAt = if (completedSuccessfully) finishedAt else it.lastSuccessAt,
                    isRunning = false
                )
            }
            _status.value
        } catch (e: Exception) {
            Log.e(TAG, "Manual sync failed with an unexpected exception.", e)
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
        Log.d(TAG, "Syncing parties. pending=${pending.size}")
        val successCount = AtomicInteger(0)
        val failedCount = AtomicInteger(0)
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
                    successCount.incrementAndGet()
                } else {
                    val error = response.errorBody()?.string()
                    Log.w(TAG, "Party create failed. localId=${entity.localId}, http=${response.code()}, error=$error")
                    partyDao.insertParty(syncingEntity.copy(syncState = SyncState.failureStateFor(entity.syncState), syncError = error))
                    failedCount.incrementAndGet()
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
                    successCount.incrementAndGet()
                } else {
                    val error = response.errorBody()?.string()
                    Log.w(TAG, "Party update failed. localId=${entity.localId}, remoteId=${entity.remoteId}, http=${response.code()}, error=$error")
                    partyDao.insertParty(syncingEntity.copy(syncState = SyncState.failureStateFor(entity.syncState), syncError = error))
                    failedCount.incrementAndGet()
                }
            }
        }
        return SyncResultSummary(pushedParties = successCount.get(), failedItems = failedCount.get())
    }

    private suspend fun syncShipments(): SyncResultSummary {
        _status.update { it.copy(phase = "Subiendo envíos") }
        val pending = shipmentDao.getPendingSyncShipments()
        Log.d(TAG, "Syncing shipments. pending=${pending.size}")
        val successCount = AtomicInteger(0)
        val failedCount = AtomicInteger(0)
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
                    successCount.incrementAndGet()
                } else {
                    val error = response.errorBody()?.string()
                    Log.w(TAG, "Shipment create failed. localId=${entity.localId}, http=${response.code()}, error=$error")
                    shipmentDao.insertShipment(syncingEntity.copy(syncState = SyncState.failureStateFor(entity.syncState), syncError = error))
                    failedCount.incrementAndGet()
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
                    successCount.incrementAndGet()
                } else {
                    val error = response.errorBody()?.string()
                    Log.w(TAG, "Shipment update failed. localId=${entity.localId}, remoteId=${entity.remoteId}, http=${response.code()}, error=$error")
                    shipmentDao.insertShipment(syncingEntity.copy(syncState = SyncState.failureStateFor(entity.syncState), syncError = error))
                    failedCount.incrementAndGet()
                }
            }
        }
        return SyncResultSummary(pushedShipments = successCount.get(), failedItems = failedCount.get())
    }

    private suspend fun syncTrades(): SyncResultSummary {
        _status.update { it.copy(phase = "Subiendo negocios") }
        val pending = tradeDao.getPendingSyncTrades()
        Log.d(TAG, "Syncing trades. pending=${pending.size}")
        val successCount = AtomicInteger(0)
        val failedCount = AtomicInteger(0)
        syncConcurrent(pending, 4) { entity ->
            val now = nowIso()
            val syncingEntity = entity.copy(syncState = SyncState.SYNCING, lastSyncAttemptAt = now, syncError = null)
            tradeDao.insertTrade(syncingEntity)

            val partyRemoteId = partyDao.getPartyById(entity.partyLocalId)?.remoteId
            val shipmentRemoteId = shipmentDao.getShipmentById(entity.shipmentLocalId)?.remoteId
            if (partyRemoteId == null || shipmentRemoteId == null) {
                Log.w(
                    TAG,
                    "Trade skipped because dependencies are missing remoteId. localId=${entity.localId}, partyLocalId=${entity.partyLocalId}, partyRemoteId=$partyRemoteId, shipmentLocalId=${entity.shipmentLocalId}, shipmentRemoteId=$shipmentRemoteId"
                )
                tradeDao.insertTrade(syncingEntity.copy(syncState = SyncState.failureStateFor(entity.syncState), syncError = "Dependencias sin remoteId"))
                failedCount.incrementAndGet()
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
                successCount.incrementAndGet()
            } else {
                val error = response.errorBody()?.string()
                Log.w(TAG, "Trade sync failed. localId=${entity.localId}, remoteId=${entity.remoteId}, http=${response.code()}, error=$error")
                tradeDao.insertTrade(syncingEntity.copy(syncState = SyncState.failureStateFor(entity.syncState), syncError = error))
                failedCount.incrementAndGet()
            }
        }
        return SyncResultSummary(pushedTrades = successCount.get(), failedItems = failedCount.get())
    }

    private suspend fun syncSelections(): SyncResultSummary {
        _status.update { it.copy(phase = "Subiendo selecciones") }
        val pending = selectionDao.getPendingSelections()
        Log.d(TAG, "Syncing selections. pending=${pending.size}")
        val successCount = AtomicInteger(0)
        val failedCount = AtomicInteger(0)
        syncConcurrent(pending, 6) { selectionWithWeights ->
            val entity = selectionWithWeights.selection
            val now = nowIso()
            val syncingSelection = entity.copy(syncState = SyncState.SYNCING, lastSyncAttemptAt = now, syncError = null)
            selectionDao.updateSelection(syncingSelection)

            val tradeRemoteId = tradeDao.getTradeById(entity.tradeLocalId)?.remoteId
            if (tradeRemoteId == null) {
                Log.w(TAG, "Selection skipped because trade has no remoteId. localId=${entity.localId}, tradeLocalId=${entity.tradeLocalId}")
                selectionDao.updateSelection(syncingSelection.copy(syncState = SyncState.failureStateFor(entity.syncState), syncError = "Negocio sin remoteId"))
                failedCount.incrementAndGet()
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
                    val updatedSelection = syncingSelection.copy(
                        remoteId = remoteSelection.selectionByTradeId,
                        price = remoteSelection.price?.toDoubleOrNull(),
                        selectionTypeName = remoteSelection.selectionType?.nameSelection,
                        syncState = SyncState.CLEAN,
                        lastSyncedAt = now,
                        serverUpdatedAt = remoteSelection.updatedAt,
                        syncError = null
                    )
                    selectionDao.updateSelection(updatedSelection)
                    
                    selectionDao.deleteUnitWeightsBySelectionId(updatedSelection.localId)
                    selectionDao.insertUnitWeights(
                        remoteSelection.unitWeights.orEmpty().map { unitWeight ->
                            UnitWeightEntity(
                                localId = unitWeight.unitWeightId,
                                remoteId = unitWeight.unitWeightId,
                                selectionLocalId = updatedSelection.localId,
                                weight = unitWeight.weight.toDoubleOrNull() ?: 0.0,
                                amount = unitWeight.amount
                            )
                        }
                    )
                }
                successCount.incrementAndGet()
            } else {
                val error = response.errorBody()?.string()
                Log.w(TAG, "Selection sync failed. localId=${entity.localId}, remoteId=${entity.remoteId}, http=${response.code()}, error=$error")
                selectionDao.updateSelection(syncingSelection.copy(syncState = SyncState.failureStateFor(entity.syncState), syncError = error))
                failedCount.incrementAndGet()
            }
        }
        return SyncResultSummary(pushedSelections = successCount.get(), failedItems = failedCount.get())
    }

    private suspend fun syncApuntes(): SyncResultSummary {
        _status.update { it.copy(phase = "Subiendo apuntes") }
        val pending = apuntesDao.getPendingSyncApuntes()
        Log.d(TAG, "Syncing apuntes. pending=${pending.size}")
        val successCount = AtomicInteger(0)
        val failedCount = AtomicInteger(0)
        syncConcurrent(pending, 4) { recordWithDetails ->
            val entity = recordWithDetails.apunte
            val now = nowIso()
            val syncingEntity = entity.copy(syncState = SyncState.SYNCING, lastSyncAttemptAt = now, syncError = null)
            apuntesDao.insertApunte(syncingEntity)

            val response = if (entity.remoteId == null) {
                apuntesApiService.createApunte(recordWithDetails.toCreateRequest())
            } else {
                apuntesApiService.updateApunte(entity.remoteId, recordWithDetails.toCreateRequest())
            }
            if (response.isSuccessful && response.body() != null) {
                val remote = response.body()!!.record
                apuntesDao.replaceRecordWithDetails(
                    remote.toEntity(localId = entity.localId, now = nowIso()),
                    remote.details.orEmpty().map { it.toEntity(entity.localId) }
                )
                successCount.incrementAndGet()
            } else {
                val error = response.errorBody()?.string() ?: "HTTP ${response.code()}"
                Log.w(TAG, "Apunte sync failed. localId=${entity.localId}, remoteId=${entity.remoteId}, http=${response.code()}, error=$error")
                apuntesDao.insertApunte(syncingEntity.copy(syncState = SyncState.failureStateFor(entity.syncState), syncError = error))
                failedCount.incrementAndGet()
            }
        }
        return SyncResultSummary(pushedApuntes = successCount.get(), failedItems = failedCount.get())
    }

    private suspend fun pullParties(): String? {
        _status.update { it.copy(phase = "Actualizando contactos") }
        val response = partiesApiService.getParties(updatedAfter = sessionDataStore.getLastPartySync())
        val body = response.body()
        if (!response.isSuccessful || body == null) {
            return response.toPullErrorMessage("contactos")
        }
        val now = nowIso()
        body.parties.forEach { dto -> upsertRemoteParty(dto, now) }
        sessionDataStore.saveLastPartySync(now)
        return null
    }

    private suspend fun pullShipments(): String? {
        _status.update { it.copy(phase = "Actualizando envíos") }
        val response = shipmentsApiService.getShipments(updatedAfter = sessionDataStore.getLastShipmentSync())
        val body = response.body()
        if (!response.isSuccessful || body == null) {
            return response.toPullErrorMessage("envíos")
        }
        val now = nowIso()
        body.shipments.forEach { dto -> upsertRemoteShipment(dto, now) }
        sessionDataStore.saveLastShipmentSync(now)
        return null
    }

    private suspend fun pullTrades(): String? {
        _status.update { it.copy(phase = "Actualizando negocios") }
        val lastSync = sessionDataStore.getLastTradeSync() ?: "2000-01-01T00:00:00Z"
        val response = tradesApiService.getTrades(shipmentId = null, updatedAfter = lastSync)
        val body = response.body()
        if (!response.isSuccessful || body == null) {
            return response.toPullErrorMessage("negocios")
        }
        val now = nowIso()
        body.trades.forEach { dto -> upsertRemoteTrade(dto, now) }
        sessionDataStore.saveLastTradeSync(now)
        return null
    }

    private suspend fun pullSelections(): String? {
        _status.update { it.copy(phase = "Actualizando selecciones") }
        val response = selectionsApiService.getSelections(updatedAfter = sessionDataStore.getLastSelectionSync())
        val body = response.body()
        if (!response.isSuccessful || body == null) {
            return response.toPullErrorMessage("selecciones")
        }
        val now = nowIso()
        database.withTransaction {
            body.selections.forEach { dto -> upsertRemoteSelection(dto, now) }
        }
        sessionDataStore.saveLastSelectionSync(now)
        return null
    }

    private suspend fun pullApuntes(): String? {
        _status.update { it.copy(phase = "Actualizando apuntes") }
        val response = apuntesApiService.getApuntes()
        val body = response.body()
        if (!response.isSuccessful || body == null) {
            val message = response.toPullErrorMessage("apuntes")
            Log.w(TAG, "Pull apuntes failed. $message")
            return message
        }
        val now = nowIso()
        body.records.forEach { dto ->
            val existing = apuntesDao.getApunteByRemoteId(dto.id)
            if (existing == null || existing.syncState == SyncState.CLEAN) {
                apuntesDao.replaceRecordWithDetails(
                    dto.toEntity(localId = existing?.localId ?: 0, now = now),
                    dto.details.orEmpty().map { it.toEntity(existing?.localId ?: 0) }
                )
            }
        }
        return null
    }

    private suspend fun upsertRemoteParty(dto: PartyDto, now: String) {
        val existing = partyDao.getPartyByRemoteId(dto.partyId)
        if (existing != null && SyncState.isPending(existing.syncState)) {
            if (hasRemoteConflict(existing.serverUpdatedAt, dto.updatedAt)) {
                partyDao.insertParty(existing.asConflict("El contacto también cambió en el servidor. Revisa antes de sincronizar."))
            }
            return
        }
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
        if (existing != null && SyncState.isPending(existing.syncState)) {
            if (hasRemoteConflict(existing.serverUpdatedAt, dto.updatedAt)) {
                shipmentDao.insertShipment(existing.asConflict("El envío también cambió en el servidor. Revisa antes de sincronizar."))
            }
            return
        }
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
        if (existing != null && SyncState.isPending(existing.syncState)) {
            if (hasRemoteConflict(existing.serverUpdatedAt, dto.updatedAt)) {
                tradeDao.insertTrade(existing.asConflict("El negocio también cambió en el servidor. Revisa antes de sincronizar."))
            }
            return
        }
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
        if (existing != null && SyncState.isPending(existing.selection.syncState)) {
            if (hasRemoteConflict(existing.selection.serverUpdatedAt, dto.updatedAt)) {
                selectionDao.updateSelection(existing.selection.asConflict("La selección también cambió en el servidor. Revisa antes de sincronizar."))
            }
            return
        }

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

    private fun hasRemoteConflict(localServerUpdatedAt: String?, remoteUpdatedAt: String?): Boolean {
        return !localServerUpdatedAt.isNullOrBlank() &&
            !remoteUpdatedAt.isNullOrBlank() &&
            localServerUpdatedAt != remoteUpdatedAt
    }

    private fun PartyEntity.asConflict(message: String): PartyEntity {
        return copy(
            syncState = SyncState.CONFLICT,
            syncError = message
        )
    }

    private fun ShipmentEntity.asConflict(message: String): ShipmentEntity {
        return copy(
            syncState = SyncState.CONFLICT,
            syncError = message
        )
    }

    private fun TradeEntity.asConflict(message: String): TradeEntity {
        return copy(
            syncState = SyncState.CONFLICT,
            syncError = message
        )
    }

    private fun SelectionEntity.asConflict(message: String): SelectionEntity {
        return copy(
            syncState = SyncState.CONFLICT,
            syncError = message
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
            pushedApuntes = pushedApuntes + other.pushedApuntes,
            failedItems = failedItems + other.failedItems
        )
    }

    private fun buildSyncMessage(
        completedSuccessfully: Boolean,
        pullErrors: List<String>,
        remainingPending: Int
    ): String {
        if (completedSuccessfully) return "Sincronización completada."
        if (pullErrors.isNotEmpty() && remainingPending > 0) {
            return "Sincronización incompleta: quedan $remainingPending datos pendientes; ${pullErrors.joinToString("; ")}."
        }
        if (pullErrors.isNotEmpty()) return "Sincronización incompleta: ${pullErrors.joinToString("; ")}."
        if (remainingPending > 0) return "Sincronización incompleta: quedan $remainingPending datos pendientes."

        return "Sincronización completada con fallos parciales."
    }

    private suspend fun getTotalPendingCount(): Int {
        return partyDao.getPendingCount() +
            shipmentDao.getPendingCount() +
            tradeDao.getPendingCount() +
            selectionDao.getPendingCount() +
            apuntesDao.getPendingCount()
    }

    private fun <T> Response<T>.toPullErrorMessage(entityName: String): String {
        val errorDetail = errorBody()?.string()?.takeIf { it.isNotBlank() }
        return if (isSuccessful) {
            "no se pudieron actualizar $entityName porque el servidor respondió vacío"
        } else {
            buildString {
                append("no se pudieron actualizar ")
                append(entityName)
                append(" (HTTP ")
                append(code())
                append(")")
                if (errorDetail != null) {
                    append(": ")
                    append(errorDetail.take(180))
                }
            }
        }
    }

    private fun nowIso(): String = syncTimestampFormat.format(Date())

    private fun ApunteWithDetails.toCreateRequest(): CreateApunteRequestDto {
        return CreateApunteRequestDto(
            observations = apunte.observations.orEmpty(),
            details = details.map {
                CreateApunteDetailRequestDto(
                    selectionTypeId = it.selectionTypeId,
                    jabaCount = it.jabaCount,
                    isEnabled = it.isEnabled
                )
            }
        )
    }

    companion object {
        private const val TAG = "ManualSyncManager"
    }
}
