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
import aimar.rojas.avmadmin.features.shipments.data.CreateShipmentExpenseRequest
import aimar.rojas.avmadmin.features.shipments.data.CreateShipmentLaborRequest
import aimar.rojas.avmadmin.features.shipments.data.ShipmentExpenseDto
import aimar.rojas.avmadmin.features.shipments.data.ShipmentExpensesApiService
import aimar.rojas.avmadmin.features.shipments.data.ShipmentLaborApiService
import aimar.rojas.avmadmin.features.shipments.data.ShipmentLaborDto
import aimar.rojas.avmadmin.features.shipments.data.ShipmentDto
import aimar.rojas.avmadmin.features.shipments.data.ShipmentsApiService
import aimar.rojas.avmadmin.features.shipments.data.UpdateShipmentExpenseRequest
import aimar.rojas.avmadmin.features.shipments.data.UpdateShipmentLaborRequest
import aimar.rojas.avmadmin.features.shipments.data.local.ShipmentDao
import aimar.rojas.avmadmin.features.shipments.data.local.ShipmentExpenseDao
import aimar.rojas.avmadmin.features.shipments.data.local.ShipmentLaborDao
import aimar.rojas.avmadmin.features.shipments.data.local.entities.ShipmentEntity
import aimar.rojas.avmadmin.features.shipments.data.local.entities.ShipmentExpenseEntity
import aimar.rojas.avmadmin.features.shipments.data.local.entities.ShipmentLaborEntity
import aimar.rojas.avmadmin.features.trades.data.CreateTradeRequest
import aimar.rojas.avmadmin.features.trades.data.TradeDto
import aimar.rojas.avmadmin.features.trades.data.TradesApiService
import aimar.rojas.avmadmin.features.trades.data.UpdateTradeRequest
import aimar.rojas.avmadmin.features.trades.data.local.TradeDao
import aimar.rojas.avmadmin.features.trades.data.local.entities.TradeEntity
import aimar.rojas.avmadmin.features.workers.data.CreateWorkerRequest
import aimar.rojas.avmadmin.features.workers.data.UpdateWorkerRequest
import aimar.rojas.avmadmin.features.workers.data.WorkerDto
import aimar.rojas.avmadmin.features.workers.data.WorkersApiService
import aimar.rojas.avmadmin.features.workers.data.local.WorkerDao
import aimar.rojas.avmadmin.features.workers.data.local.entities.WorkerEntity
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
    private val workerDao: WorkerDao,
    private val shipmentDao: ShipmentDao,
    private val shipmentExpenseDao: ShipmentExpenseDao,
    private val shipmentLaborDao: ShipmentLaborDao,
    private val tradeDao: TradeDao,
    private val selectionDao: SelectionDao,
    private val apuntesDao: ApuntesDao,
    private val partiesApiService: PartiesApiService,
    private val workersApiService: WorkersApiService,
    private val shipmentsApiService: ShipmentsApiService,
    private val shipmentExpensesApiService: ShipmentExpensesApiService,
    private val shipmentLaborApiService: ShipmentLaborApiService,
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
                workerDao.observePendingCount(),
                shipmentDao.observePendingCount(),
                shipmentExpenseDao.observePendingCount(),
                shipmentLaborDao.observePendingCount(),
                tradeDao.observePendingCount(),
                selectionDao.observePendingCount(),
                apuntesDao.observePendingCount()
            ) { values ->
                SyncEntitySummary(
                    partyPending = values[0],
                    workerPending = values[1],
                    shipmentPending = values[2],
                    shipmentExpensePending = values[3],
                    shipmentLaborPending = values[4],
                    tradePending = values[5],
                    selectionPending = values[6],
                    apuntePending = values[7]
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
            pullWorkers()?.let { pullErrors.add(it) }
            pullShipments()?.let { pullErrors.add(it) }
            pullShipmentExpenses()?.let { pullErrors.add(it) }
            pullShipmentLabor()?.let { pullErrors.add(it) }
            pullTrades()?.let { pullErrors.add(it) }
            pullSelections()?.let { pullErrors.add(it) }
            pullApuntes()?.let { pullErrors.add(it) }

            resultSummary = resultSummary.merge(syncParties())
            resultSummary = resultSummary.merge(syncWorkers())
            resultSummary = resultSummary.merge(syncShipments())
            resultSummary = resultSummary.merge(syncShipmentExpenses())
            resultSummary = resultSummary.merge(syncShipmentLabor())
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

    private suspend fun syncWorkers(): SyncResultSummary {
        _status.update { it.copy(phase = "Subiendo personal") }
        val pending = workerDao.getPendingSyncWorkers()
        Log.d(TAG, "Syncing workers. pending=${pending.size}")
        val successCount = AtomicInteger(0)
        val failedCount = AtomicInteger(0)
        syncConcurrent(pending, 4) { entity ->
            val now = nowIso()
            val syncingEntity = entity.copy(syncState = SyncState.SYNCING, lastSyncAttemptAt = now, syncError = null)
            workerDao.insertWorker(syncingEntity)

            val response = if (entity.remoteId == null) {
                workersApiService.createWorker(
                    CreateWorkerRequest(
                        fullName = entity.fullName,
                        dni = entity.dni,
                        phone = entity.phone,
                        notes = entity.notes
                    )
                )
            } else {
                workersApiService.updateWorker(
                    entity.remoteId,
                    UpdateWorkerRequest(
                        fullName = entity.fullName,
                        dni = entity.dni,
                        phone = entity.phone,
                        isActive = entity.isActive,
                        notes = entity.notes
                    )
                )
            }

            if (response.isSuccessful && response.body() != null) {
                val dto = response.body()!!.worker
                val updated = syncingEntity.mergeRemote(dto, now)
                workerDao.insertWorker(updated)
                successCount.incrementAndGet()
            } else {
                val error = response.errorBody()?.string()
                Log.w(TAG, "Worker sync failed. localId=${entity.localId}, remoteId=${entity.remoteId}, http=${response.code()}, error=$error")
                workerDao.insertWorker(syncingEntity.copy(syncState = SyncState.failureStateFor(entity.syncState), syncError = error))
                failedCount.incrementAndGet()
            }
        }
        return SyncResultSummary(pushedWorkers = successCount.get(), failedItems = failedCount.get())
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

    private suspend fun syncShipmentExpenses(): SyncResultSummary {
        _status.update { it.copy(phase = "Subiendo costos de envío") }
        val pending = shipmentExpenseDao.getPendingSyncExpenses()
        Log.d(TAG, "Syncing shipment expenses. pending=${pending.size}")
        val successCount = AtomicInteger(0)
        val failedCount = AtomicInteger(0)
        syncConcurrent(pending, 4) { entity ->
            val now = nowIso()
            val syncingEntity = entity.copy(syncState = SyncState.SYNCING, lastSyncAttemptAt = now, syncError = null)
            shipmentExpenseDao.insertExpense(syncingEntity)

            val shipmentRemoteId = shipmentDao.getShipmentById(entity.shipmentLocalId)?.remoteId
            if (shipmentRemoteId == null) {
                Log.w(TAG, "Shipment expense skipped because shipment has no remoteId. localId=${entity.localId}, shipmentLocalId=${entity.shipmentLocalId}")
                shipmentExpenseDao.insertExpense(syncingEntity.copy(syncState = SyncState.failureStateFor(entity.syncState), syncError = "Envío sin remoteId"))
                failedCount.incrementAndGet()
                return@syncConcurrent
            }

            val response = if (entity.remoteId == null) {
                shipmentExpensesApiService.createExpense(
                    CreateShipmentExpenseRequest(
                        shipmentId = shipmentRemoteId,
                        category = entity.category,
                        subcategory = entity.subcategory,
                        amount = entity.amount,
                        quantity = entity.quantity,
                        unitPrice = entity.unitPrice,
                        description = entity.description,
                        expenseDate = entity.expenseDate,
                        paidByPartyId = entity.paidByPartyLocalId?.let { partyDao.getPartyById(it)?.remoteId }
                    )
                )
            } else {
                shipmentExpensesApiService.updateExpense(
                    entity.remoteId,
                    UpdateShipmentExpenseRequest(
                        category = entity.category,
                        subcategory = entity.subcategory,
                        amount = entity.amount,
                        quantity = entity.quantity,
                        unitPrice = entity.unitPrice,
                        description = entity.description,
                        expenseDate = entity.expenseDate,
                        paidByPartyId = entity.paidByPartyLocalId?.let { partyDao.getPartyById(it)?.remoteId }
                    )
                )
            }

            if (response.isSuccessful && response.body() != null) {
                val dto = response.body()!!.expense
                val updated = syncingEntity.mergeRemote(dto, now)
                shipmentExpenseDao.insertExpense(updated)
                successCount.incrementAndGet()
            } else {
                val error = response.errorBody()?.string()
                Log.w(TAG, "Shipment expense sync failed. localId=${entity.localId}, remoteId=${entity.remoteId}, http=${response.code()}, error=$error")
                shipmentExpenseDao.insertExpense(syncingEntity.copy(syncState = SyncState.failureStateFor(entity.syncState), syncError = error))
                failedCount.incrementAndGet()
            }
        }
        return SyncResultSummary(pushedShipmentExpenses = successCount.get(), failedItems = failedCount.get())
    }

    private suspend fun syncShipmentLabor(): SyncResultSummary {
        _status.update { it.copy(phase = "Subiendo jornales") }
        val pending = shipmentLaborDao.getPendingSyncLabor()
        Log.d(TAG, "Syncing shipment labor. pending=${pending.size}")
        val successCount = AtomicInteger(0)
        val failedCount = AtomicInteger(0)
        syncConcurrent(pending, 4) { entity ->
            val now = nowIso()
            val syncingEntity = entity.copy(syncState = SyncState.SYNCING, lastSyncAttemptAt = now, syncError = null)
            shipmentLaborDao.insertLabor(syncingEntity)

            val shipmentRemoteId = shipmentDao.getShipmentById(entity.shipmentLocalId)?.remoteId
            val workerRemoteId = workerDao.getWorkerById(entity.workerLocalId)?.remoteId
            if (shipmentRemoteId == null || workerRemoteId == null) {
                Log.w(
                    TAG,
                    "Shipment labor skipped because dependencies are missing remoteId. localId=${entity.localId}, shipmentLocalId=${entity.shipmentLocalId}, shipmentRemoteId=$shipmentRemoteId, workerLocalId=${entity.workerLocalId}, workerRemoteId=$workerRemoteId"
                )
                shipmentLaborDao.insertLabor(syncingEntity.copy(syncState = SyncState.failureStateFor(entity.syncState), syncError = "Dependencias sin remoteId"))
                failedCount.incrementAndGet()
                return@syncConcurrent
            }

            val response = if (entity.remoteId == null) {
                shipmentLaborApiService.createLabor(
                    CreateShipmentLaborRequest(
                        shipmentId = shipmentRemoteId,
                        workerId = workerRemoteId,
                        workDate = entity.workDate,
                        amount = entity.amount,
                        notes = entity.notes
                    )
                )
            } else {
                shipmentLaborApiService.updateLabor(
                    entity.remoteId,
                    UpdateShipmentLaborRequest(
                        workDate = entity.workDate,
                        amount = entity.amount,
                        notes = entity.notes
                    )
                )
            }

            if (response.isSuccessful && response.body() != null) {
                val dto = response.body()!!.labor
                val updated = syncingEntity.mergeRemote(dto, now)
                shipmentLaborDao.insertLabor(updated)
                successCount.incrementAndGet()
            } else {
                val error = response.errorBody()?.string()
                Log.w(TAG, "Shipment labor sync failed. localId=${entity.localId}, remoteId=${entity.remoteId}, http=${response.code()}, error=$error")
                shipmentLaborDao.insertLabor(syncingEntity.copy(syncState = SyncState.failureStateFor(entity.syncState), syncError = error))
                failedCount.incrementAndGet()
            }
        }
        return SyncResultSummary(pushedShipmentLabor = successCount.get(), failedItems = failedCount.get())
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
        val response = partiesApiService.getParties()
        val body = response.body()
        if (!response.isSuccessful || body == null) {
            return response.toPullErrorMessage("contactos")
        }
        val now = nowIso()
        body.parties.forEach { dto -> upsertRemoteParty(dto, now) }
        sessionDataStore.saveLastPartySync(now)
        return null
    }

    private suspend fun pullWorkers(): String? {
        _status.update { it.copy(phase = "Actualizando personal") }
        val response = workersApiService.getWorkers()
        val body = response.body()
        if (!response.isSuccessful || body == null) {
            return response.toPullErrorMessage("personal")
        }
        val now = nowIso()
        body.workers.forEach { dto -> upsertRemoteWorker(dto, now) }
        sessionDataStore.saveLastWorkerSync(now)
        return null
    }

    private suspend fun pullShipments(): String? {
        _status.update { it.copy(phase = "Actualizando envíos") }
        val now = nowIso()
        var page = 1
        do {
            val response = shipmentsApiService.getShipments(
                page = page,
                limit = PULL_PAGE_LIMIT
            )
            val body = response.body()
            if (!response.isSuccessful || body == null) {
                return response.toPullErrorMessage("envíos")
            }
            body.shipments.forEach { dto -> upsertRemoteShipment(dto, now) }
            page += 1
        } while (body.hasNext)
        sessionDataStore.saveLastShipmentSync(now)
        return null
    }

    private suspend fun pullShipmentExpenses(): String? {
        _status.update { it.copy(phase = "Actualizando costos de envío") }
        val response = shipmentExpensesApiService.getExpenses()
        val body = response.body()
        if (!response.isSuccessful || body == null) {
            return response.toPullErrorMessage("costos de envío")
        }
        val now = nowIso()
        body.expenses.forEach { dto -> upsertRemoteShipmentExpense(dto, now) }
        sessionDataStore.saveLastShipmentExpenseSync(now)
        return null
    }

    private suspend fun pullShipmentLabor(): String? {
        _status.update { it.copy(phase = "Actualizando jornales") }
        val response = shipmentLaborApiService.getLabor()
        val body = response.body()
        if (!response.isSuccessful || body == null) {
            return response.toPullErrorMessage("jornales")
        }
        val now = nowIso()
        body.labor.forEach { dto -> upsertRemoteShipmentLabor(dto, now) }
        sessionDataStore.saveLastShipmentLaborSync(now)
        return null
    }

    private suspend fun pullTrades(): String? {
        _status.update { it.copy(phase = "Actualizando negocios") }
        val now = nowIso()
        var page = 1
        do {
            val response = tradesApiService.getTrades(
                shipmentId = null,
                page = page,
                limit = PULL_PAGE_LIMIT,
                updatedAfter = INITIAL_SYNC_UPDATED_AFTER
            )
            val body = response.body()
            if (!response.isSuccessful || body == null) {
                return response.toPullErrorMessage("negocios")
            }
            body.trades.forEach { dto -> upsertRemoteTrade(dto, now) }
            page += 1
        } while (body.hasNext)
        sessionDataStore.saveLastTradeSync(now)
        return null
    }

    private suspend fun pullSelections(): String? {
        _status.update { it.copy(phase = "Actualizando selecciones") }
        val response = selectionsApiService.getSelections()
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

    private suspend fun upsertRemoteWorker(dto: WorkerDto, now: String) {
        val existing = workerDao.getWorkerByRemoteId(dto.id)
        if (existing != null && SyncState.isPending(existing.syncState)) {
            if (hasRemoteConflict(existing.serverUpdatedAt, dto.updatedAt)) {
                workerDao.insertWorker(existing.asConflict("El personal también cambió en el servidor. Revisa antes de sincronizar."))
            }
            return
        }
        workerDao.insertWorker(
            WorkerEntity(
                localId = existing?.localId ?: dto.id,
                remoteId = dto.id,
                fullName = dto.fullName,
                dni = dto.dni,
                phone = dto.phone,
                isActive = dto.isActive,
                notes = dto.notes,
                syncState = SyncState.CLEAN,
                lastSyncedAt = now,
                serverUpdatedAt = dto.updatedAt,
                syncError = null
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

    private suspend fun upsertRemoteShipmentExpense(dto: ShipmentExpenseDto, now: String) {
        val shipmentLocalId = shipmentDao.getShipmentByRemoteId(dto.shipmentId)?.localId ?: return
        val existing = shipmentExpenseDao.getExpenseByRemoteId(dto.id)
        if (existing != null && SyncState.isPending(existing.syncState)) {
            if (hasRemoteConflict(existing.serverUpdatedAt, dto.updatedAt)) {
                shipmentExpenseDao.insertExpense(existing.asConflict("El costo también cambió en el servidor. Revisa antes de sincronizar."))
            }
            return
        }

        val paidByPartyLocalId = dto.paidByPartyId?.let { partyDao.getPartyByRemoteId(it)?.localId }
        shipmentExpenseDao.insertExpense(
            ShipmentExpenseEntity(
                localId = existing?.localId ?: dto.id,
                remoteId = dto.id,
                shipmentLocalId = shipmentLocalId,
                category = dto.category,
                subcategory = dto.subcategory,
                amount = dto.amount,
                quantity = dto.quantity,
                unitPrice = dto.unitPrice,
                description = dto.description,
                expenseDate = dto.expenseDate,
                paidByPartyLocalId = paidByPartyLocalId,
                syncState = SyncState.CLEAN,
                lastSyncedAt = now,
                serverUpdatedAt = dto.updatedAt,
                syncError = null
            )
        )
    }

    private suspend fun upsertRemoteShipmentLabor(dto: ShipmentLaborDto, now: String) {
        dto.worker?.let { upsertRemoteWorker(it, now) }

        val shipmentLocalId = shipmentDao.getShipmentByRemoteId(dto.shipmentId)?.localId ?: return
        val workerLocalId = workerDao.getWorkerByRemoteId(dto.workerId)?.localId ?: return
        val existing = shipmentLaborDao.getLaborByRemoteId(dto.id)
        if (existing != null && SyncState.isPending(existing.syncState)) {
            if (hasRemoteConflict(existing.serverUpdatedAt, dto.updatedAt)) {
                shipmentLaborDao.insertLabor(existing.asConflict("El jornal también cambió en el servidor. Revisa antes de sincronizar."))
            }
            return
        }

        shipmentLaborDao.insertLabor(
            ShipmentLaborEntity(
                localId = existing?.localId ?: dto.id,
                remoteId = dto.id,
                shipmentLocalId = shipmentLocalId,
                workerLocalId = workerLocalId,
                workDate = dto.workDate,
                amount = dto.amount,
                notes = dto.notes,
                syncState = SyncState.CLEAN,
                lastSyncedAt = now,
                serverUpdatedAt = dto.updatedAt,
                syncError = null
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

    private suspend fun WorkerEntity.mergeRemote(dto: WorkerDto, now: String): WorkerEntity {
        return copy(
            remoteId = dto.id,
            fullName = dto.fullName,
            dni = dto.dni,
            phone = dto.phone,
            isActive = dto.isActive,
            notes = dto.notes,
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

    private suspend fun ShipmentExpenseEntity.mergeRemote(dto: ShipmentExpenseDto, now: String): ShipmentExpenseEntity {
        val shipmentLocalId = shipmentDao.getShipmentByRemoteId(dto.shipmentId)?.localId ?: shipmentLocalId
        val paidByPartyLocalId = dto.paidByPartyId?.let { partyDao.getPartyByRemoteId(it)?.localId }
        return copy(
            remoteId = dto.id,
            shipmentLocalId = shipmentLocalId,
            category = dto.category,
            subcategory = dto.subcategory,
            amount = dto.amount,
            quantity = dto.quantity,
            unitPrice = dto.unitPrice,
            description = dto.description,
            expenseDate = dto.expenseDate,
            paidByPartyLocalId = paidByPartyLocalId,
            syncState = SyncState.CLEAN,
            lastSyncedAt = now,
            serverUpdatedAt = dto.updatedAt,
            syncError = null
        )
    }

    private suspend fun ShipmentLaborEntity.mergeRemote(dto: ShipmentLaborDto, now: String): ShipmentLaborEntity {
        dto.worker?.let { upsertRemoteWorker(it, now) }
        val shipmentLocalId = shipmentDao.getShipmentByRemoteId(dto.shipmentId)?.localId ?: shipmentLocalId
        val workerLocalId = workerDao.getWorkerByRemoteId(dto.workerId)?.localId ?: workerLocalId
        return copy(
            remoteId = dto.id,
            shipmentLocalId = shipmentLocalId,
            workerLocalId = workerLocalId,
            workDate = dto.workDate,
            amount = dto.amount,
            notes = dto.notes,
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

    private fun WorkerEntity.asConflict(message: String): WorkerEntity {
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

    private fun ShipmentExpenseEntity.asConflict(message: String): ShipmentExpenseEntity {
        return copy(
            syncState = SyncState.CONFLICT,
            syncError = message
        )
    }

    private fun ShipmentLaborEntity.asConflict(message: String): ShipmentLaborEntity {
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
            pushedWorkers = pushedWorkers + other.pushedWorkers,
            pushedShipments = pushedShipments + other.pushedShipments,
            pushedShipmentExpenses = pushedShipmentExpenses + other.pushedShipmentExpenses,
            pushedShipmentLabor = pushedShipmentLabor + other.pushedShipmentLabor,
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
            workerDao.getPendingCount() +
            shipmentDao.getPendingCount() +
            shipmentExpenseDao.getPendingCount() +
            shipmentLaborDao.getPendingCount() +
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
        private const val PULL_PAGE_LIMIT = 200
        private const val INITIAL_SYNC_UPDATED_AFTER = "2000-01-01T00:00:00Z"
    }
}
