package aimar.rojas.avmadmin.features.shipments.data

import aimar.rojas.avmadmin.features.workers.data.WorkerDto
import com.google.gson.annotations.SerializedName

data class ShipmentLaborDto(
    val id: Int,
    @SerializedName("shipment_id")
    val shipmentId: Int,
    @SerializedName("worker_id")
    val workerId: Int,
    val worker: WorkerDto? = null,
    @SerializedName("work_date")
    val workDate: String,
    val amount: Double,
    val notes: String? = null,
    @SerializedName("created_at")
    val createdAt: String? = null,
    @SerializedName("updated_at")
    val updatedAt: String? = null
)

data class ShipmentLaborResponseDto(
    val labor: List<ShipmentLaborDto>,
    val total: Int
)

data class CreateShipmentLaborRequest(
    @SerializedName("shipment_id")
    val shipmentId: Int,
    @SerializedName("worker_id")
    val workerId: Int,
    @SerializedName("work_date")
    val workDate: String,
    val amount: Double,
    val notes: String? = null
)

data class UpdateShipmentLaborRequest(
    @SerializedName("work_date")
    val workDate: String? = null,
    val amount: Double? = null,
    val notes: String? = null
)

data class ShipmentLaborItemResponseDto(
    val message: String,
    val labor: ShipmentLaborDto
)
