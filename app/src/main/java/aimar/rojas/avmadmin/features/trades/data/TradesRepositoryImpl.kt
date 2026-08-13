package aimar.rojas.avmadmin.features.trades.data

import aimar.rojas.avmadmin.core.sync.SyncState
import aimar.rojas.avmadmin.core.data.local.AvmDatabase
import aimar.rojas.avmadmin.domain.model.Trade
import aimar.rojas.avmadmin.features.trades.data.local.TradeDao
import aimar.rojas.avmadmin.features.trades.data.local.entities.TradeEntity
import aimar.rojas.avmadmin.features.trades.domain.TradesRepository
import aimar.rojas.avmadmin.features.trades.domain.TradesResult
import aimar.rojas.avmadmin.features.selections.data.local.SelectionDao
import androidx.room.withTransaction
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class TradesRepositoryImpl @Inject constructor(
    private val database: AvmDatabase,
    private val tradeDao: TradeDao,
    private val selectionDao: SelectionDao,
    private val tradesApiService: TradesApiService
) : TradesRepository {

    override suspend fun getTrades(
        shipmentId: Int,
        page: Int,
        limit: Int,
        tradeType: String?
    ): Result<TradesResult> {
        return try {
            val allLocal = tradeDao.getTradesByShipment(shipmentId).first()
            val filtered = allLocal.filter { t ->
                tradeType == null || t.tradeType.equals(tradeType, ignoreCase = true)
            }

            val offset = (page - 1) * limit
            val paginated = filtered.drop(offset).take(limit).map { it.toDomain() }
            val totalPages = (filtered.size + limit - 1) / limit
            Result.success(
                TradesResult(
                    trades = paginated,
                    total = filtered.size.toLong(),
                    page = page,
                    limit = limit,
                    totalPages = totalPages,
                    hasNext = page < totalPages,
                    hasPrevious = page > 1
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createTrade(
        partyId: Int,
        shipmentId: Int,
        tradeType: String,
        startDatetime: String,
        endDatetime: String?,
        discountWeightPerTray: Double,
        varietyAvocado: String
    ): Result<Trade> {
        return try {
            val localTrade = TradeEntity(
                partyLocalId = partyId,
                bossId = 0,
                shipmentLocalId = shipmentId,
                tradeType = tradeType,
                startDatetime = startDatetime,
                endDatetime = endDatetime,
                discountWeightPerTray = discountWeightPerTray,
                varietyAvocado = varietyAvocado,
                amountPerTrade = 0.0,
                syncState = SyncState.PENDING_CREATE
            )
            val localId = tradeDao.insertTrade(localTrade).toInt()
            Result.success(tradeDao.getTradeById(localId)?.toDomain() ?: localTrade.copy(localId = localId).toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getTradeById(tradeId: Int): Result<Trade> {
        return try {
            val entity = tradeDao.getTradeById(tradeId)
            if (entity != null) {
                Result.success(entity.toDomain())
            } else {
                Result.failure(Exception("Trade not found locally"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteTrade(tradeId: Int): Result<Unit> {
        return try {
            val entity = tradeDao.getTradeById(tradeId)
                ?: return Result.failure(Exception("No se encontró la transacción"))

            if (entity.remoteId != null && entity.syncState != SyncState.PENDING_CREATE) {
                val response = tradesApiService.deleteTrade(entity.remoteId)
                if (!response.isSuccessful && response.code() != 404) {
                    val error = response.errorBody()?.string()
                    return Result.failure(Exception(error ?: "No se pudo eliminar en el servidor"))
                }
            }

            database.withTransaction {
                selectionDao.deleteSelectionsByTradeId(tradeId)
                tradeDao.deleteTradeById(tradeId)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
