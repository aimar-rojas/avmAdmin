package aimar.rojas.avmadmin.features.shipments.data.local

import aimar.rojas.avmadmin.features.shipments.data.local.entities.ShipmentEntity
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ShipmentDao {
    @Query("SELECT * FROM shipments ORDER BY startDate DESC")
    fun getShipments(): Flow<List<ShipmentEntity>>

    @Query("SELECT * FROM shipments ORDER BY startDate DESC")
    suspend fun getShipmentsList(): List<ShipmentEntity>

    @Query("SELECT * FROM shipments WHERE localId = :shipmentId")
    suspend fun getShipmentById(shipmentId: Int): ShipmentEntity?

    @Query("SELECT * FROM shipments WHERE remoteId = :remoteId LIMIT 1")
    suspend fun getShipmentByRemoteId(remoteId: Int): ShipmentEntity?

    @Query("SELECT * FROM shipments WHERE syncState != 'CLEAN' ORDER BY startDate DESC")
    suspend fun getPendingSyncShipments(): List<ShipmentEntity>

    @Query("SELECT COUNT(*) FROM shipments WHERE syncState != 'CLEAN'")
    fun observePendingCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShipment(shipment: ShipmentEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShipments(shipments: List<ShipmentEntity>)

    @Query("SELECT COUNT(*) FROM shipments")
    suspend fun getShipmentCount(): Int
}
