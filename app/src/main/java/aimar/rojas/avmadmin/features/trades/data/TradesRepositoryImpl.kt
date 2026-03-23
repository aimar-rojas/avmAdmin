package aimar.rojas.avmadmin.features.trades.data

import aimar.rojas.avmadmin.core.workers.AvmSyncWorker
import aimar.rojas.avmadmin.data.local.SessionDataStore
import aimar.rojas.avmadmin.domain.model.Trade
import aimar.rojas.avmadmin.features.trades.data.local.TradeDao
import aimar.rojas.avmadmin.features.trades.data.local.entities.TradeEntity
import aimar.rojas.avmadmin.features.trades.domain.TradesRepository
import aimar.rojas.avmadmin.features.trades.domain.TradesResult
import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import retrofit2.HttpException
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

class TradesRepositoryImpl @Inject constructor(
    private val tradesApiService: TradesApiService,
    private val tradeDao: TradeDao,
    private val sessionDataStore: SessionDataStore,
    @ApplicationContext private val context: Context
) : TradesRepository {

    override suspend fun getTrades(
        shipmentId: Int,
        page: Int,
        limit: Int,
        tradeType: String?
    ): Result<TradesResult> {
        return try {
            val pending = tradeDao.getPendingSyncTrades()
            if (pending.isEmpty()) {
                val lastSync = sessionDataStore.getLastTradeSync()
                val response = tradesApiService.getTrades(
                    shipmentId = shipmentId,
                    page = page,
                    limit = limit,
                    tradeType = tradeType,
                    updatedAfter = lastSync
                )
                
                if (response.isSuccessful && response.body() != null) {
                    val serverTrades = response.body()!!.trades
                    if (serverTrades.isNotEmpty()) {
                        val entities = serverTrades.map { dto ->
                            TradeEntity(
                                tradeId = dto.tradeId,
                                partyId = dto.partyId,
                                bossId = dto.bossId,
                                shipmentId = dto.shipmentId,
                                tradeType = dto.tradeType,
                                startDatetime = dto.startDatetime,
                                endDatetime = dto.endDatetime,
                                discountWeightPerTray = dto.discountWeightPerTray,
                                amountPerTrade = dto.amountPerTrade,
                                isPendingSync = false,
                                syncOperation = null
                            )
                        }
                        tradeDao.insertTrades(entities)
                        
                        val currentTime = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC") }.format(Date())
                        sessionDataStore.saveLastTradeSync(currentTime)
                    }
                }
            }
            fetchFromLocalDB(shipmentId, page, limit, tradeType)
        } catch (e: Exception) {
            fetchFromLocalDB(shipmentId, page, limit, tradeType)
        }
    }

    private suspend fun fetchFromLocalDB(
        shipmentId: Int,
        page: Int,
        limit: Int,
        tradeType: String?
    ): Result<TradesResult> {
        return try {
            val allLocal = tradeDao.getTradesByShipment(shipmentId).first()
            
            val filtered = allLocal.filter { t ->
                (tradeType == null || t.tradeType.equals(tradeType, ignoreCase = true))
            }
            
            val offset = (page - 1) * limit
            val paginated = filtered.drop(offset).take(limit).map { it.toDomain() }
            
            val totalPages = (filtered.size + limit - 1) / limit
            Result.success(TradesResult(
                trades = paginated,
                total = filtered.size.toLong(),
                page = page,
                limit = limit,
                totalPages = totalPages,
                hasNext = page < totalPages,
                hasPrevious = page > 1
            ))
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
        Log.d("AvmAdminSync", "Starting local createTrade... (partyId=$partyId, shipmentId=$shipmentId)")
        return try {
            val tempId = -(System.currentTimeMillis() % Int.MAX_VALUE).toInt()
            
            val localTrade = TradeEntity(
                tradeId = tempId,
                partyId = partyId,
                bossId = 0, // DTO doesn't require boss_id in CreateTradeRequest so we set 0
                shipmentId = shipmentId,
                tradeType = tradeType,
                startDatetime = startDatetime,
                endDatetime = endDatetime,
                discountWeightPerTray = discountWeightPerTray,
                amountPerTrade = 0.0,
                isPendingSync = true,
                syncOperation = "CREATE"
            )
            tradeDao.insertTrade(localTrade)
            Log.d("AvmAdminSync", "Local trade saved in SQLite with TempID=$tempId")
            
            Log.d("AvmAdminSync", "Attempting to enqueue sync worker...")
            enqueueSyncWorker()
            
            Log.d("AvmAdminSync", "Trade creation process completed successfully.")
            Result.success(localTrade.toDomain())
        } catch (e: Exception) {
            Log.e("AvmAdminSync", "Error creating trade locally", e)
            Result.failure(e)
        }
    }
    private fun enqueueSyncWorker() {
        Log.d("AvmAdminSync", "enqueueSyncWorker() triggered in TradesRepositoryImpl")
        try {
            val workRequest = OneTimeWorkRequestBuilder<AvmSyncWorker>()
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .build()
            WorkManager.getInstance(context).enqueueUniqueWork(
                "SyncWork",
                ExistingWorkPolicy.REPLACE,
                workRequest
            )
            Log.d("AvmAdminSync", "Worker successfully enqueued to system!")
        } catch (e: Exception) {
            Log.e("AvmAdminSync", "Error enqueuing worker", e)
        }
    }
}
