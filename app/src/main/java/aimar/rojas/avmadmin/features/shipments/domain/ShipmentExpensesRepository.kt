package aimar.rojas.avmadmin.features.shipments.domain

import aimar.rojas.avmadmin.features.shipments.domain.model.ShipmentExpense
import kotlinx.coroutines.flow.Flow

interface ShipmentExpensesRepository {
    fun observeExpenses(shipmentId: Int): Flow<List<ShipmentExpense>>

    suspend fun createExpense(
        shipmentId: Int,
        category: String,
        subcategory: String?,
        amount: Double,
        quantity: Double?,
        unitPrice: Double?,
        description: String?,
        expenseDate: String,
        paidByPartyId: Int? = null
    ): Result<ShipmentExpense>
}
