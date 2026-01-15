package aimar.rojas.avmadmin.domain.repository

import aimar.rojas.avmadmin.domain.model.Trade

interface TradeRepository {
    suspend fun getTrades(): List<Trade>
    suspend fun addTrade(trade: Trade)
    suspend fun editTrade(trade: Trade)
}