package aimar.rojas.avmadmin.features.selections.data.local.entities

import aimar.rojas.avmadmin.core.sync.SyncState
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "selections",
    indices = [
        Index(value = ["remoteId"], unique = true),
        Index(value = ["tradeLocalId", "selectionTypeId"], unique = true)
    ]
)
data class SelectionEntity(
    @PrimaryKey(autoGenerate = true)
    val localId: Int = 0,
    val remoteId: Int? = null,
    val tradeLocalId: Int,
    val selectionTypeId: Int,
    val price: Double?,
    val selectionTypeName: String?,
    val syncState: String = SyncState.CLEAN,
    val lastSyncAttemptAt: String? = null,
    val lastSyncedAt: String? = null,
    val serverUpdatedAt: String? = null,
    val syncError: String? = null
)
