package aimar.rojas.avmadmin.features.shipments.data.local

import aimar.rojas.avmadmin.features.shipments.data.local.entities.ShipmentExpenseEntity
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ShipmentExpenseDao {
    @Query("SELECT * FROM shipment_expenses WHERE shipmentLocalId = :shipmentId ORDER BY expenseDate DESC, localId DESC")
    fun observeExpensesByShipment(shipmentId: Int): Flow<List<ShipmentExpenseEntity>>

    @Query("SELECT * FROM shipment_expenses WHERE shipmentLocalId = :shipmentId ORDER BY expenseDate DESC, localId DESC")
    suspend fun getExpensesByShipment(shipmentId: Int): List<ShipmentExpenseEntity>

    @Query("SELECT * FROM shipment_expenses WHERE remoteId = :remoteId LIMIT 1")
    suspend fun getExpenseByRemoteId(remoteId: Int): ShipmentExpenseEntity?

    @Query("SELECT * FROM shipment_expenses WHERE syncState != 'CLEAN' AND syncState != 'CONFLICT' ORDER BY localId ASC")
    suspend fun getPendingSyncExpenses(): List<ShipmentExpenseEntity>

    @Query("SELECT * FROM shipment_expenses WHERE syncState != 'CLEAN' ORDER BY localId ASC")
    fun observePendingSyncExpenses(): Flow<List<ShipmentExpenseEntity>>

    @Query("SELECT COUNT(*) FROM shipment_expenses WHERE syncState != 'CLEAN'")
    fun observePendingCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM shipment_expenses WHERE syncState != 'CLEAN'")
    suspend fun getPendingCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ShipmentExpenseEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpenses(expenses: List<ShipmentExpenseEntity>)
}
