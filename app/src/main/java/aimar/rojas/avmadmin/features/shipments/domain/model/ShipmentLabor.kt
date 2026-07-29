package aimar.rojas.avmadmin.features.shipments.domain.model

data class ShipmentLabor(
    val laborId: Int,
    val remoteId: Int? = null,
    val shipmentId: Int,
    val workerId: Int,
    val workerName: String,
    val workDate: String,
    val amount: Double,
    val notes: String? = null,
    val syncState: String? = null,
    val syncError: String? = null
)
