package aimar.rojas.avmadmin.features.trades.data.local

import aimar.rojas.avmadmin.features.trades.data.local.entities.TradeEntity
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TradeDao {
    @Query("SELECT * FROM trades WHERE shipmentId = :shipmentId AND (isPendingSync = 0 OR syncOperation != 'DELETE') ORDER BY startDatetime DESC")
    fun getTradesByShipment(shipmentId: Int): Flow<List<TradeEntity>>

    @Query("SELECT * FROM trades WHERE tradeId = :tradeId")
    suspend fun getTradeById(tradeId: Int): TradeEntity?

    @Query("SELECT * FROM trades WHERE isPendingSync = 1")
    suspend fun getPendingSyncTrades(): List<TradeEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrade(trade: TradeEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrades(trades: List<TradeEntity>)

    @Query("UPDATE trades SET tradeId = :newId, isPendingSync = 0, syncOperation = NULL WHERE tradeId = :oldId")
    suspend fun updateTradeIdAndMarkSynced(oldId: Int, newId: Int)

    @Query("UPDATE trades SET partyId = :newPartyId WHERE partyId = :oldPartyId")
    suspend fun updateForeignPartyId(oldPartyId: Int, newPartyId: Int)

    @Query("UPDATE trades SET shipmentId = :newShipmentId WHERE shipmentId = :oldShipmentId")
    suspend fun updateForeignShipmentId(oldShipmentId: Int, newShipmentId: Int)

    @Query("DELETE FROM trades WHERE isPendingSync = 0")
    suspend fun clearSyncedTrades()
}
