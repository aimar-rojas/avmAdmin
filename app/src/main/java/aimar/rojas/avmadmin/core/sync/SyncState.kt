package aimar.rojas.avmadmin.core.sync

object SyncState {
    const val CLEAN = "CLEAN"
    const val PENDING_CREATE = "PENDING_CREATE"
    const val PENDING_UPDATE = "PENDING_UPDATE"
    const val SYNCING = "SYNCING"
    const val CONFLICT = "CONFLICT"
    const val FAILED_CREATE = "FAILED_CREATE"
    const val FAILED_UPDATE = "FAILED_UPDATE"

    fun isPending(state: String): Boolean = state != CLEAN

    fun failureStateFor(currentState: String): String {
        return when (currentState) {
            PENDING_CREATE, FAILED_CREATE -> FAILED_CREATE
            else -> FAILED_UPDATE
        }
    }
}
