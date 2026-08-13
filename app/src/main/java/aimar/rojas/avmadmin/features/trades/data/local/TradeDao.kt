package aimar.rojas.avmadmin.features.trades.data.local

import aimar.rojas.avmadmin.features.trades.data.local.entities.TradeEntity
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TradeDao {
    @Query("SELECT * FROM trades WHERE shipmentLocalId = :shipmentId ORDER BY startDatetime DESC")
    fun getTradesByShipment(shipmentId: Int): Flow<List<TradeEntity>>

    @Query("SELECT * FROM trades WHERE shipmentLocalId = :shipmentId ORDER BY startDatetime DESC")
    suspend fun getTradesByShipmentList(shipmentId: Int): List<TradeEntity>

    @Query("SELECT * FROM trades WHERE localId = :tradeId")
    suspend fun getTradeById(tradeId: Int): TradeEntity?

    @Query("SELECT * FROM trades WHERE remoteId = :remoteId LIMIT 1")
    suspend fun getTradeByRemoteId(remoteId: Int): TradeEntity?

    @Query("SELECT * FROM trades WHERE syncState != 'CLEAN' AND syncState != 'CONFLICT' ORDER BY localId ASC")
    suspend fun getPendingSyncTrades(): List<TradeEntity>

    @Query("SELECT * FROM trades WHERE syncState != 'CLEAN' ORDER BY localId ASC")
    fun observePendingSyncTrades(): Flow<List<TradeEntity>>

    @Query("SELECT COUNT(*) FROM trades WHERE syncState != 'CLEAN'")
    fun observePendingCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM trades WHERE syncState != 'CLEAN'")
    suspend fun getPendingCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrade(trade: TradeEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrades(trades: List<TradeEntity>)

    @Query("SELECT COUNT(*) FROM trades")
    suspend fun getTradeCount(): Int

    @Query("DELETE FROM trades WHERE localId = :tradeId")
    suspend fun deleteTradeById(tradeId: Int)
}
