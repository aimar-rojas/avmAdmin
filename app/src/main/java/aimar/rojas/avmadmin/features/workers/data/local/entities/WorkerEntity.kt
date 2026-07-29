package aimar.rojas.avmadmin.features.workers.data.local.entities

import aimar.rojas.avmadmin.core.sync.SyncState
import aimar.rojas.avmadmin.features.workers.domain.model.Worker
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "workers",
    indices = [
        Index(value = ["remoteId"], unique = true),
        Index(value = ["fullName"]),
        Index(value = ["isActive"])
    ]
)
data class WorkerEntity(
    @PrimaryKey(autoGenerate = true)
    val localId: Int = 0,
    val remoteId: Int? = null,
    val fullName: String,
    val dni: String? = null,
    val phone: String? = null,
    val isActive: Boolean = true,
    val notes: String? = null,
    val syncState: String = SyncState.CLEAN,
    val lastSyncAttemptAt: String? = null,
    val lastSyncedAt: String? = null,
    val serverUpdatedAt: String? = null,
    val syncError: String? = null
) {
    fun toDomain(): Worker {
        return Worker(
            workerId = localId,
            remoteId = remoteId,
            fullName = fullName,
            dni = dni,
            phone = phone,
            isActive = isActive,
            notes = notes,
            syncState = syncState,
            syncError = syncError
        )
    }
}
