package aimar.rojas.avmadmin.features.shipments.data.local.entities

import aimar.rojas.avmadmin.core.sync.SyncState
import aimar.rojas.avmadmin.domain.model.Shipment
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "shipments",
    indices = [Index(value = ["remoteId"], unique = true)]
)
data class ShipmentEntity(
    @PrimaryKey(autoGenerate = true)
    val localId: Int = 0,
    val remoteId: Int? = null,
    val startDate: Date,
    val endDate: Date?,
    val status: String,
    val amountPerShipment: Double,
    val syncState: String = SyncState.CLEAN,
    val lastSyncAttemptAt: String? = null,
    val lastSyncedAt: String? = null,
    val serverUpdatedAt: String? = null,
    val syncError: String? = null
) {
    fun toDomain(): Shipment {
        return Shipment(
            shipmentId = localId,
            remoteId = remoteId,
            startDate = startDate,
            endDate = endDate,
            status = status,
            amountPerShipment = amountPerShipment,
            syncState = syncState
        )
    }
}

fun Shipment.toEntity(syncState: String = SyncState.CLEAN): ShipmentEntity {
    return ShipmentEntity(
        localId = this.shipmentId,
        remoteId = this.remoteId,
        startDate = this.startDate,
        endDate = this.endDate,
        status = this.status,
        amountPerShipment = this.amountPerShipment,
        syncState = syncState
    )
}
