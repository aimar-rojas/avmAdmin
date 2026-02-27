package aimar.rojas.avmadmin.features.trades.domain

import aimar.rojas.avmadmin.domain.model.Trade

interface TradesRepository {
    suspend fun getTrades(
        shipmentId: Int,
        page: Int = 1,
        limit: Int = 50,
        tradeType: String? = null
    ): Result<TradesResult>
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
