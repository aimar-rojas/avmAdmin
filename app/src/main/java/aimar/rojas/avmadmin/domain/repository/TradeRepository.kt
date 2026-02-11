package aimar.rojas.avmadmin.domain.repository

import aimar.rojas.avmadmin.domain.model.Trade

interface TradeRepository {
    suspend fun getTrades(
        shipmentId: Int,
        page: Int = 1,
        limit: Int = 50,
        tradeType: String? = null
    ): Result<TradesResult>
    suspend fun addTrade(trade: Trade)
    suspend fun editTrade(trade: Trade)
}

data class TradesResult(
    val trades: List<Trade>,
    val total: Long,
    val page: Int,
    val limit: Int,
    val totalPages: Int,
    val hasNext: Boolean,
    val hasPrevious: Boolean
)