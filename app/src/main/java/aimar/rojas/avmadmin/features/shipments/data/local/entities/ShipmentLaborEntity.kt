package aimar.rojas.avmadmin.features.shipments.data.local.entities

import aimar.rojas.avmadmin.core.sync.SyncState
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "shipment_labor",
    foreignKeys = [
        ForeignKey(
            entity = ShipmentEntity::class,
            parentColumns = ["localId"],
            childColumns = ["shipmentLocalId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = aimar.rojas.avmadmin.features.workers.data.local.entities.WorkerEntity::class,
            parentColumns = ["localId"],
            childColumns = ["workerLocalId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["remoteId"], unique = true),
        Index(value = ["shipmentLocalId"]),
        Index(value = ["workerLocalId"]),
        Index(value = ["workDate"]),
        Index(value = ["shipmentLocalId", "workerLocalId", "workDate"], unique = true)
    ]
)
data class ShipmentLaborEntity(
    @PrimaryKey(autoGenerate = true)
    val localId: Int = 0,
    val remoteId: Int? = null,
    val shipmentLocalId: Int,
    val workerLocalId: Int,
    val workDate: String,
    val amount: Double,
    val notes: String? = null,
    val syncState: String = SyncState.CLEAN,
    val lastSyncAttemptAt: String? = null,
    val lastSyncedAt: String? = null,
    val serverUpdatedAt: String? = null,
    val syncError: String? = null
)
