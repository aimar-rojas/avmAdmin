package aimar.rojas.avmadmin.features.apuntes.data.local.entities

import aimar.rojas.avmadmin.core.sync.SyncState
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(
    tableName = "apuntes",
    indices = [Index(value = ["remoteId"], unique = true)]
)
data class ApunteEntity(
    @PrimaryKey(autoGenerate = true)
    val localId: Int = 0,
    val remoteId: Int? = null,
    val userId: Int = 0,
    val authorName: String? = null,
    val recordDate: String,
    val observations: String?,
    val syncState: String = SyncState.CLEAN,
    val lastSyncAttemptAt: String? = null,
    val lastSyncedAt: String? = null,
    val serverUpdatedAt: String? = null,
    val syncError: String? = null
)

@Entity(
    tableName = "apunte_details",
    foreignKeys = [
        ForeignKey(
            entity = ApunteEntity::class,
            parentColumns = ["localId"],
            childColumns = ["apunteLocalId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["apunteLocalId"]),
        Index(value = ["remoteId"], unique = true)
    ]
)
data class ApunteDetailEntity(
    @PrimaryKey(autoGenerate = true)
    val localId: Int = 0,
    val remoteId: Int? = null,
    val apunteLocalId: Int,
    val selectionTypeId: Int,
    val jabaCount: Int,
    val isEnabled: Boolean
)

data class ApunteWithDetails(
    @Embedded val apunte: ApunteEntity,
    @Relation(
        parentColumn = "localId",
        entityColumn = "apunteLocalId"
    )
    val details: List<ApunteDetailEntity>
)
