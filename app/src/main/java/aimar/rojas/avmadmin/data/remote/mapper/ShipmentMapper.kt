package aimar.rojas.avmadmin.data.remote.mapper

import aimar.rojas.avmadmin.data.remote.dto.ShipmentDto
import aimar.rojas.avmadmin.domain.model.Shipment
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun ShipmentDto.toDomain(): Shipment {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    
    return Shipment(
        shipmentId = shipmentId,
        startDate = dateFormat.parse(startDate) ?: Date(),
        endDate = endDate?.let { dateFormat.parse(it) },
        status = status,
        amountPerShipment = 0.0
    )
}

fun Date.toApiFormat(): String {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return dateFormat.format(this)
}
