package aimar.rojas.avmadmin.features.shipments.data

import aimar.rojas.avmadmin.core.sync.SyncState
import aimar.rojas.avmadmin.features.shipments.data.local.ShipmentExpenseDao
import aimar.rojas.avmadmin.features.shipments.data.local.entities.ShipmentExpenseEntity
import aimar.rojas.avmadmin.features.shipments.domain.ShipmentExpensesRepository
import aimar.rojas.avmadmin.features.shipments.domain.model.ShipmentExpense
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ShipmentExpensesRepositoryImpl @Inject constructor(
    private val expenseDao: ShipmentExpenseDao
) : ShipmentExpensesRepository {

    override fun observeExpenses(shipmentId: Int): Flow<List<ShipmentExpense>> {
        return expenseDao.observeExpensesByShipment(shipmentId)
            .map { expenses -> expenses.map { it.toDomain() } }
    }

    override suspend fun createExpense(
        shipmentId: Int,
        category: String,
        subcategory: String?,
        amount: Double,
        quantity: Double?,
        unitPrice: Double?,
        description: String?,
        expenseDate: String,
        paidByPartyId: Int?
    ): Result<ShipmentExpense> {
        return try {
            val expense = ShipmentExpenseEntity(
                shipmentLocalId = shipmentId,
                category = category,
                subcategory = subcategory,
                amount = amount,
                quantity = quantity,
                unitPrice = unitPrice,
                description = description,
                expenseDate = expenseDate,
                paidByPartyLocalId = paidByPartyId,
                syncState = SyncState.PENDING_CREATE
            )
            val localId = expenseDao.insertExpense(expense).toInt()
            Result.success(expense.copy(localId = localId).toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
