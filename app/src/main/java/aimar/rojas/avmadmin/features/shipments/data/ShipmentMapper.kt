package aimar.rojas.avmadmin.features.shipments.data

import aimar.rojas.avmadmin.domain.model.Shipment
import aimar.rojas.avmadmin.utils.DateUtils
import java.util.Date

fun ShipmentDto.toDomain(): Shipment {
    return Shipment(
        shipmentId = shipmentId,
        startDate = DateUtils.parseApiDate(startDate) ?: Date(),
        endDate = DateUtils.parseApiDate(endDate),
        status = status,
        amountPerShipment = 0.0
    )
}

fun Date.toApiFormat(): String {
    return DateUtils.formatToApiDate(this)
}
