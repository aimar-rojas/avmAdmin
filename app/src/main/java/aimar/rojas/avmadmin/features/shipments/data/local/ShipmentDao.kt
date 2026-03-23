package aimar.rojas.avmadmin.features.shipments.data.local

import aimar.rojas.avmadmin.features.shipments.data.local.entities.ShipmentEntity
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ShipmentDao {
    @Query("SELECT * FROM shipments WHERE isPendingSync = 0 OR syncOperation != 'DELETE' ORDER BY startDate DESC")
    fun getShipments(): Flow<List<ShipmentEntity>>

    @Query("SELECT * FROM shipments WHERE shipmentId = :shipmentId")
    suspend fun getShipmentById(shipmentId: Int): ShipmentEntity?

    @Query("SELECT * FROM shipments WHERE isPendingSync = 1")
    suspend fun getPendingSyncShipments(): List<ShipmentEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShipment(shipment: ShipmentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShipments(shipments: List<ShipmentEntity>)

    @Query("UPDATE shipments SET shipmentId = :newId, isPendingSync = 0, syncOperation = NULL WHERE shipmentId = :oldId")
    suspend fun updateShipmentIdAndMarkSynced(oldId: Int, newId: Int)

    @Query("DELETE FROM shipments WHERE isPendingSync = 0")
    suspend fun clearSyncedShipments()
}
