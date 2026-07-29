package aimar.rojas.avmadmin.features.sync.presentation

import aimar.rojas.avmadmin.core.sync.ManualSyncManager
import aimar.rojas.avmadmin.features.apuntes.data.local.ApuntesDao
import aimar.rojas.avmadmin.features.apuntes.data.local.entities.ApunteWithDetails
import aimar.rojas.avmadmin.features.parties.data.local.PartyDao
import aimar.rojas.avmadmin.features.parties.data.local.entities.PartyEntity
import aimar.rojas.avmadmin.features.selections.data.local.SelectionDao
import aimar.rojas.avmadmin.features.selections.data.local.entities.SelectionEntity
import aimar.rojas.avmadmin.features.shipments.data.local.ShipmentDao
import aimar.rojas.avmadmin.features.shipments.data.local.ShipmentExpenseDao
import aimar.rojas.avmadmin.features.shipments.data.local.ShipmentLaborDao
import aimar.rojas.avmadmin.features.shipments.data.local.entities.ShipmentEntity
import aimar.rojas.avmadmin.features.shipments.data.local.entities.ShipmentExpenseEntity
import aimar.rojas.avmadmin.features.shipments.data.local.entities.ShipmentLaborEntity
import aimar.rojas.avmadmin.features.shipments.presentation.components.expenseCategoryLabel
import aimar.rojas.avmadmin.features.trades.data.local.TradeDao
import aimar.rojas.avmadmin.features.trades.data.local.entities.TradeEntity
import aimar.rojas.avmadmin.features.workers.data.local.WorkerDao
import aimar.rojas.avmadmin.features.workers.data.local.entities.WorkerEntity
import aimar.rojas.avmadmin.utils.DateUtils
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PendingSyncUiState(
    val items: List<PendingSyncItem> = emptyList(),
    val isSyncing: Boolean = false
)

data class PendingSyncItem(
    val id: String,
    val entityLabel: String,
    val title: String,
    val subtitle: String,
    val syncState: String,
    val error: String?,
    val lastAttemptAt: String?
) {
    val hasError: Boolean
        get() = syncState.startsWith("FAILED") || !error.isNullOrBlank()
}

private data class CorePendingItems(
    val parties: List<PartyEntity>,
    val shipments: List<ShipmentEntity>,
    val trades: List<TradeEntity>,
    val selections: List<SelectionEntity>,
    val apuntes: List<ApunteWithDetails>
)

@HiltViewModel
class PendingSyncViewModel @Inject constructor(
    private val partyDao: PartyDao,
    private val workerDao: WorkerDao,
    private val shipmentDao: ShipmentDao,
    private val shipmentExpenseDao: ShipmentExpenseDao,
    private val shipmentLaborDao: ShipmentLaborDao,
    private val tradeDao: TradeDao,
    private val selectionDao: SelectionDao,
    private val apuntesDao: ApuntesDao,
    private val manualSyncManager: ManualSyncManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(PendingSyncUiState())
    val uiState: StateFlow<PendingSyncUiState> = _uiState.asStateFlow()

    init {
        observePendingItems()
        observeSyncStatus()
    }

    fun retrySync() {
        viewModelScope.launch {
            manualSyncManager.syncNow()
        }
    }

    private fun observePendingItems() {
        viewModelScope.launch {
            val corePendingFlow = combine(
                partyDao.observePendingSyncParties(),
                shipmentDao.observePendingSyncShipments(),
                tradeDao.observePendingSyncTrades(),
                selectionDao.observePendingSelectionEntities(),
                apuntesDao.observePendingApuntes()
            ) { parties, shipments, trades, selections, apuntes ->
                CorePendingItems(
                    parties = parties,
                    shipments = shipments,
                    trades = trades,
                    selections = selections,
                    apuntes = apuntes
                )
            }

            combine(
                corePendingFlow,
                workerDao.observePendingSyncWorkers(),
                shipmentExpenseDao.observePendingSyncExpenses(),
                shipmentLaborDao.observePendingSyncLabor()
            ) { core, workers, expenses, labor ->
                buildList {
                    addAll(core.parties.map { it.toPendingItem() })
                    addAll(workers.map { it.toPendingItem() })
                    addAll(core.shipments.map { it.toPendingItem() })
                    addAll(expenses.map { it.toPendingItem() })
                    addAll(labor.map { it.toPendingItem() })
                    addAll(core.trades.map { it.toPendingItem() })
                    addAll(core.selections.map { it.toPendingItem() })
                    addAll(core.apuntes.map { it.toPendingItem() })
                }.sortedWith(
                    compareByDescending<PendingSyncItem> { it.hasError }
                        .thenBy { it.entityLabel }
                        .thenBy { it.title }
                )
            }.collect { items ->
                _uiState.update { it.copy(items = items) }
            }
        }
    }

    private fun observeSyncStatus() {
        viewModelScope.launch {
            manualSyncManager.status.collect { status ->
                _uiState.update { it.copy(isSyncing = status.isRunning) }
            }
        }
    }

    private fun PartyEntity.toPendingItem(): PendingSyncItem {
        val name = aliasName?.takeIf { it.isNotBlank() }
            ?: listOf(firstName, lastName).filterNot { it.isNullOrBlank() }.joinToString(" ")
                .ifBlank { "Contacto #$localId" }
        val role = if (partyRole == "producer") "Productor" else "Comprador"

        return PendingSyncItem(
            id = "party-$localId",
            entityLabel = role,
            title = name,
            subtitle = if (remoteId == null) "Nuevo contacto por subir" else "Contacto modificado",
            syncState = syncState,
            error = syncError,
            lastAttemptAt = DateUtils.formatSyncTimestampToDisplay(lastSyncAttemptAt)
        )
    }

    private fun ShipmentEntity.toPendingItem(): PendingSyncItem {
        return PendingSyncItem(
            id = "shipment-$localId",
            entityLabel = "Envío",
            title = "Envío ${DateUtils.formatToDisplayDate(startDate)}",
            subtitle = if (remoteId == null) "Nuevo envío por subir" else "Envío modificado",
            syncState = syncState,
            error = syncError,
            lastAttemptAt = DateUtils.formatSyncTimestampToDisplay(lastSyncAttemptAt)
        )
    }

    private fun WorkerEntity.toPendingItem(): PendingSyncItem {
        return PendingSyncItem(
            id = "worker-$localId",
            entityLabel = "Personal",
            title = fullName,
            subtitle = if (remoteId == null) "Nuevo personal por subir" else "Personal modificado",
            syncState = syncState,
            error = syncError,
            lastAttemptAt = DateUtils.formatSyncTimestampToDisplay(lastSyncAttemptAt)
        )
    }

    private fun ShipmentExpenseEntity.toPendingItem(): PendingSyncItem {
        return PendingSyncItem(
            id = "shipment-expense-$localId",
            entityLabel = "Costo",
            title = expenseCategoryLabel(category),
            subtitle = "Envío local #$shipmentLocalId - S/ ${"%.2f".format(amount)}",
            syncState = syncState,
            error = syncError,
            lastAttemptAt = DateUtils.formatSyncTimestampToDisplay(lastSyncAttemptAt)
        )
    }

    private fun ShipmentLaborEntity.toPendingItem(): PendingSyncItem {
        val displayWorkDate = DateUtils.convertApiToDisplayDate(workDate) ?: workDate
        return PendingSyncItem(
            id = "shipment-labor-$localId",
            entityLabel = "Jornal",
            title = "Personal local #$workerLocalId",
            subtitle = "Envío local #$shipmentLocalId - $displayWorkDate - S/ ${"%.2f".format(amount)}",
            syncState = syncState,
            error = syncError,
            lastAttemptAt = DateUtils.formatSyncTimestampToDisplay(lastSyncAttemptAt)
        )
    }

    private fun TradeEntity.toPendingItem(): PendingSyncItem {
        return PendingSyncItem(
            id = "trade-$localId",
            entityLabel = "Negocio",
            title = "Negocio #$localId",
            subtitle = "${tradeType.toReadableTradeType()} - $varietyAvocado",
            syncState = syncState,
            error = syncError,
            lastAttemptAt = DateUtils.formatSyncTimestampToDisplay(lastSyncAttemptAt)
        )
    }

    private fun SelectionEntity.toPendingItem(): PendingSyncItem {
        return PendingSyncItem(
            id = "selection-$localId",
            entityLabel = "Selección",
            title = selectionTypeName ?: "Selección $selectionTypeId",
            subtitle = "Negocio local #$tradeLocalId",
            syncState = syncState,
            error = syncError,
            lastAttemptAt = DateUtils.formatSyncTimestampToDisplay(lastSyncAttemptAt)
        )
    }

    private fun ApunteWithDetails.toPendingItem(): PendingSyncItem {
        val enabledDetails = details.filter { it.isEnabled }
        val totalJabas = enabledDetails.sumOf { it.jabaCount }
        return PendingSyncItem(
            id = "apunte-${apunte.localId}",
            entityLabel = "Apunte",
            title = "Apunte #${apunte.localId}",
            subtitle = if (totalJabas > 0) {
                "${enabledDetails.size} selecciones, $totalJabas jabas"
            } else {
                "Apunte rápido pendiente"
            },
            syncState = apunte.syncState,
            error = apunte.syncError,
            lastAttemptAt = DateUtils.formatSyncTimestampToDisplay(apunte.lastSyncAttemptAt)
        )
    }

    private fun String.toReadableTradeType(): String {
        return when (this) {
            "buy", "BUY", "PURCHASE" -> "Compra"
            "sell", "SELL", "SALE" -> "Venta"
            else -> this
        }
    }
}
