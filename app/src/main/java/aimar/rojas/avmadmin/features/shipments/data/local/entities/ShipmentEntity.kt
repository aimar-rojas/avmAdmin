package aimar.rojas.avmadmin.features.shipments.data.local.entities

import aimar.rojas.avmadmin.domain.model.Shipment
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "shipments")
data class ShipmentEntity(
    @PrimaryKey
    val shipmentId: Int,
    val startDate: Date,
    val endDate: Date?,
    val status: String,
    val amountPerShipment: Double,
    val isPendingSync: Boolean = false,
    val syncOperation: String? = null
) {
    fun toDomain(): Shipment {
        return Shipment(
            shipmentId = shipmentId,
            startDate = startDate,
            endDate = endDate,
            status = status,
            amountPerShipment = amountPerShipment
        )
    }
}

fun Shipment.toEntity(isPendingSync: Boolean = false, syncOperation: String? = null): ShipmentEntity {
    return ShipmentEntity(
        shipmentId = this.shipmentId,
        startDate = this.startDate,
        endDate = this.endDate,
        status = this.status,
        amountPerShipment = this.amountPerShipment,
        isPendingSync = isPendingSync,
        syncOperation = syncOperation
    )
}
