package aimar.rojas.avmadmin.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ShipmentDto(
    @SerializedName("shipment_id")
    val shipmentId: Int,
    @SerializedName("start_date")
    val startDate: String,
    @SerializedName("end_date")
    val endDate: String? = null,
    val status: String,
    @SerializedName("created_at")
    val createdAt: String? = null,
    @SerializedName("updated_at")
    val updatedAt: String? = null
)

data class ShipmentsResponseDto(
    val shipments: List<ShipmentDto>,
    val total: Long,
    val page: Int,
    val limit: Int,
    @SerializedName("total_pages")
    val totalPages: Int,
    @SerializedName("has_next")
    val hasNext: Boolean,
    @SerializedName("has_previous")
    val hasPrevious: Boolean
)

data class CreateShipmentRequest(
    @SerializedName("start_date")
    val startDate: String,
    @SerializedName("end_date")
    val endDate: String? = null,
    val status: String
)

data class UpdateShipmentRequest(
    @SerializedName("start_date")
    val startDate: String? = null,
    @SerializedName("end_date")
    val endDate: String? = null,
    val status: String? = null
)

data class CreateShipmentResponseDto(
    val message: String,
    val shipment: ShipmentDto
)

data class UpdateShipmentResponseDto(
    val message: String,
    val shipment: ShipmentDto
)
