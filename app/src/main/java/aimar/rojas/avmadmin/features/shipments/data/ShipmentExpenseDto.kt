package aimar.rojas.avmadmin.features.shipments.data

import com.google.gson.annotations.SerializedName

data class ShipmentExpenseDto(
    val id: Int,
    @SerializedName("shipment_id")
    val shipmentId: Int,
    val category: String,
    val subcategory: String? = null,
    val amount: Double,
    val quantity: Double? = null,
    @SerializedName("unit_price")
    val unitPrice: Double? = null,
    val description: String? = null,
    @SerializedName("expense_date")
    val expenseDate: String,
    @SerializedName("paid_by_party_id")
    val paidByPartyId: Int? = null,
    @SerializedName("created_at")
    val createdAt: String? = null,
    @SerializedName("updated_at")
    val updatedAt: String? = null
)

data class ShipmentExpensesResponseDto(
    val expenses: List<ShipmentExpenseDto>,
    val total: Int
)

data class CreateShipmentExpenseRequest(
    @SerializedName("shipment_id")
    val shipmentId: Int,
    val category: String,
    val subcategory: String? = null,
    val amount: Double,
    val quantity: Double? = null,
    @SerializedName("unit_price")
    val unitPrice: Double? = null,
    val description: String? = null,
    @SerializedName("expense_date")
    val expenseDate: String,
    @SerializedName("paid_by_party_id")
    val paidByPartyId: Int? = null
)

data class UpdateShipmentExpenseRequest(
    val category: String? = null,
    val subcategory: String? = null,
    val amount: Double? = null,
    val quantity: Double? = null,
    @SerializedName("unit_price")
    val unitPrice: Double? = null,
    val description: String? = null,
    @SerializedName("expense_date")
    val expenseDate: String? = null,
    @SerializedName("paid_by_party_id")
    val paidByPartyId: Int? = null
)

data class ShipmentExpenseResponseDto(
    val message: String,
    val expense: ShipmentExpenseDto
)
