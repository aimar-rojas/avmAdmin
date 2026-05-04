package aimar.rojas.avmadmin.core.sync

data class SyncEntitySummary(
    val partyPending: Int = 0,
    val shipmentPending: Int = 0,
    val tradePending: Int = 0,
    val selectionPending: Int = 0
) {
    val totalPending: Int
        get() = partyPending + shipmentPending + tradePending + selectionPending
}

data class SyncResultSummary(
    val pushedParties: Int = 0,
    val pushedShipments: Int = 0,
    val pushedTrades: Int = 0,
    val pushedSelections: Int = 0,
    val failedItems: Int = 0
)

data class SyncStatus(
    val state: String = "idle",
    val phase: String? = null,
    val summary: SyncEntitySummary = SyncEntitySummary(),
    val result: SyncResultSummary = SyncResultSummary(),
    val message: String? = null,
    val lastAttemptAt: String? = null,
    val lastSuccessAt: String? = null,
    val isRunning: Boolean = false
)
