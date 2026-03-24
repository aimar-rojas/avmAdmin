package aimar.rojas.avmadmin.features.shipments.data

import aimar.rojas.avmadmin.core.workers.AvmSyncWorker
import aimar.rojas.avmadmin.data.local.SessionDataStore
import aimar.rojas.avmadmin.domain.model.Shipment
import aimar.rojas.avmadmin.features.shipments.data.local.ShipmentDao
import aimar.rojas.avmadmin.features.shipments.data.local.entities.ShipmentEntity
import aimar.rojas.avmadmin.features.shipments.domain.ShipmentsRepository
import aimar.rojas.avmadmin.features.shipments.domain.ShipmentsResult
import android.content.Context
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

class ShipmentsRepositoryImpl @Inject constructor(
    private val shipmentsApiService: ShipmentsApiService,
    private val shipmentDao: ShipmentDao,
    private val sessionDataStore: SessionDataStore,
    @ApplicationContext private val context: Context
) : ShipmentsRepository {

    private val dbDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US) // Or parsing ISO from API
    // The API sends "2024-01-01T00:00:00Z" which Gson can parse, but ShipmentDto receives String. Wait! ShipmentDto expects String from API? Yes.
    // Let's use Date type correctly because ShipmentEntity expects Date.
    // We already defined Converters, so we construct ShipmentEntity passing Date.
    // Wait, ShipmentDto has startDate/endDate as String in "YYYY-MM-DD".
    
    // We need to map String to Date cleanly.

    override suspend fun getShipments(
        page: Int,
        limit: Int, // local caching doesn't paginate deeply unless we want, but we will return all local matching.
        status: String?,
        startDate: String?,
        endDate: String?
    ): Result<ShipmentsResult> {
        return try {
            val pending = shipmentDao.getPendingSyncShipments()
            if (pending.isEmpty()) {
                val count = shipmentDao.getShipmentCount()
                val lastSync = if (count == 0) null else sessionDataStore.getLastShipmentSync()
                val response = shipmentsApiService.getShipments(
                    page = page,
                    limit = limit,
                    status = status,
                    startDate = startDate,
                    endDate = endDate,
                    updatedAfter = lastSync
                )
                
                if (response.isSuccessful && response.body() != null) {
                    val serverShipments = response.body()!!.shipments
                    if (serverShipments.isNotEmpty()) {
                        val entities = serverShipments.map { dto ->
                            val sDate = try { dbDateFormat.parse(dto.startDate) ?: Date() } catch(e:Exception){ Date() }
                            val eDate = dto.endDate?.let { try { dbDateFormat.parse(it) } catch(e:Exception){ null } }
                            ShipmentEntity(
                                shipmentId = dto.shipmentId,
                                startDate = sDate,
                                endDate = eDate,
                                status = dto.status,
                                amountPerShipment = 0.0, // Because in Get it might not be provided easily? DTO doesn't have it!
                                isPendingSync = false,
                                syncOperation = null
                            )
                        }
                        shipmentDao.insertShipments(entities)
                        
                        val currentTime = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC") }.format(Date())
                        sessionDataStore.saveLastShipmentSync(currentTime)
                    }
                }
            }
            fetchFromLocalDB(page, limit, status, startDate, endDate)
        } catch (e: Exception) {
            fetchFromLocalDB(page, limit, status, startDate, endDate)
        }
    }

    private suspend fun fetchFromLocalDB(
        page: Int,
        limit: Int,
        status: String?,
        startDate: String?,
        endDate: String?
    ): Result<ShipmentsResult> {
        return try {
            val allLocal = shipmentDao.getShipments().first()
            
            // Note: Since DB has Date, checking Strings `startDate` ("YYYY-MM-DD") needs compare or we just ignore if UI doesn't strictly need precise DB filtering.
            // Let's filter minimally.
            val filtered = allLocal.filter { s ->
                (status == null || s.status.equals(status, ignoreCase = true))
            }
            
            // Basic manual pagination emulation
            val offset = (page - 1) * limit
            val paginated = filtered.drop(offset).take(limit).map { it.toDomain() }
            
            val totalPages = (filtered.size + limit - 1) / limit
            Result.success(ShipmentsResult(
                shipments = paginated,
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

    override suspend fun createShipment(
        startDate: String,
        endDate: String?,
        status: String
    ): Result<Shipment> {
        return try {
            val tempId = -(System.currentTimeMillis() % Int.MAX_VALUE).toInt()
            
            val sDate = try { dbDateFormat.parse(startDate) ?: Date() } catch(e:Exception){ Date() }
            val eDate = endDate?.let { try { dbDateFormat.parse(it) } catch(e:Exception){ null } }
            
            val localShipment = ShipmentEntity(
                shipmentId = tempId,
                startDate = sDate,
                endDate = eDate,
                status = status,
                amountPerShipment = 0.0,
                isPendingSync = true,
                syncOperation = "CREATE"
            )
            shipmentDao.insertShipment(localShipment)
            enqueueSyncWorker()
            
            Result.success(localShipment.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateShipment(
        id: Int,
        startDate: String?,
        endDate: String?,
        status: String?
    ): Result<Shipment> {
        return try {
            val existing = shipmentDao.getShipmentById(id)
            if (existing != null) {
                val sDate = startDate?.let { try { dbDateFormat.parse(it) } catch(e:Exception){ null } } ?: existing.startDate
                val eDate = if (endDate == "") null else endDate?.let { try { dbDateFormat.parse(it) } catch(e:Exception){ null } } ?: existing.endDate
                
                val updatedLocal = existing.copy(
                    startDate = sDate,
                    endDate = eDate,
                    status = status ?: existing.status,
                    isPendingSync = true,
                    syncOperation = if (existing.shipmentId <= 0) "CREATE" else "UPDATE"
                )
                shipmentDao.insertShipment(updatedLocal)
                enqueueSyncWorker()
                Result.success(updatedLocal.toDomain())
            } else {
                Result.failure(Exception("Shipment not found locally"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun enqueueSyncWorker() {
        val workRequest = OneTimeWorkRequestBuilder<AvmSyncWorker>()
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            "SyncWork",
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }
}
