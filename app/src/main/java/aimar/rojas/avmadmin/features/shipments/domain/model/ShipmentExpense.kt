package aimar.rojas.avmadmin.features.shipments.domain.model

data class ShipmentExpense(
    val expenseId: Int,
    val remoteId: Int? = null,
    val shipmentId: Int,
    val category: String,
    val subcategory: String? = null,
    val amount: Double,
    val quantity: Double? = null,
    val unitPrice: Double? = null,
    val description: String? = null,
    val expenseDate: String,
    val paidByPartyId: Int? = null,
    val syncState: String? = null,
    val syncError: String? = null
)

object ShipmentExpenseCategory {
    const val LABOR = "LABOR"
    const val FUEL = "FUEL"
    const val FREIGHT = "FREIGHT"
    const val STEVEDORE = "STEVEDORE"
    const val VIATIC = "VIATIC"
    const val TAXI = "TAXI"
    const val OTHER = "OTHER"

    val all = listOf(LABOR, FUEL, FREIGHT, STEVEDORE, VIATIC, TAXI, OTHER)
}

object ShipmentExpenseSubcategory {
    const val BREAKFAST = "BREAKFAST"
    const val LUNCH = "LUNCH"
    const val DINNER = "DINNER"
    const val SNACK = "SNACK"
    const val LODGING = "LODGING"
    const val OTHER = "OTHER"

    val viaticOptions = listOf(BREAKFAST, LUNCH, DINNER, SNACK, LODGING, OTHER)
}
