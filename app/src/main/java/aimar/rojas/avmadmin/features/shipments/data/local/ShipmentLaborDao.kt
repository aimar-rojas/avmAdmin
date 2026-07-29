package aimar.rojas.avmadmin.features.shipments.data.local

import aimar.rojas.avmadmin.features.shipments.data.local.entities.ShipmentLaborEntity
import aimar.rojas.avmadmin.features.shipments.data.local.entities.ShipmentLaborWithWorker
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface ShipmentLaborDao {
    @Transaction
    @Query("SELECT * FROM shipment_labor WHERE shipmentLocalId = :shipmentId ORDER BY workDate DESC, localId DESC")
    fun observeLaborByShipment(shipmentId: Int): Flow<List<ShipmentLaborWithWorker>>

    @Query("SELECT * FROM shipment_labor WHERE remoteId = :remoteId LIMIT 1")
    suspend fun getLaborByRemoteId(remoteId: Int): ShipmentLaborEntity?

    @Query("SELECT * FROM shipment_labor WHERE shipmentLocalId = :shipmentId AND workerLocalId = :workerId AND workDate = :workDate LIMIT 1")
    suspend fun getLaborByWorkerDay(shipmentId: Int, workerId: Int, workDate: String): ShipmentLaborEntity?

    @Query("SELECT * FROM shipment_labor WHERE syncState != 'CLEAN' AND syncState != 'CONFLICT' ORDER BY localId ASC")
    suspend fun getPendingSyncLabor(): List<ShipmentLaborEntity>

    @Query("SELECT * FROM shipment_labor WHERE syncState != 'CLEAN' ORDER BY localId ASC")
    fun observePendingSyncLabor(): Flow<List<ShipmentLaborEntity>>

    @Query("SELECT COUNT(*) FROM shipment_labor WHERE syncState != 'CLEAN'")
    fun observePendingCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM shipment_labor WHERE syncState != 'CLEAN'")
    suspend fun getPendingCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLabor(labor: ShipmentLaborEntity): Long
}
