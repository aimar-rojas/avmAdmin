package aimar.rojas.avmadmin.features.shipments.data

import aimar.rojas.avmadmin.core.sync.SyncState
import aimar.rojas.avmadmin.domain.model.Shipment
import aimar.rojas.avmadmin.features.shipments.data.local.ShipmentDao
import aimar.rojas.avmadmin.features.shipments.data.local.entities.ShipmentEntity
import aimar.rojas.avmadmin.features.shipments.domain.ShipmentsRepository
import aimar.rojas.avmadmin.features.shipments.domain.ShipmentsResult
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class ShipmentsRepositoryImpl @Inject constructor(
    private val shipmentDao: ShipmentDao
) : ShipmentsRepository {

    private val dbDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    override suspend fun getShipments(
        page: Int,
        limit: Int,
        status: String?,
        startDate: String?,
        endDate: String?
    ): Result<ShipmentsResult> {
        return try {
            val allLocal = shipmentDao.getShipments().first()
            val filtered = allLocal.filter { s ->
                (status == null || s.status.equals(status, ignoreCase = true))
            }

            val offset = (page - 1) * limit
            val paginated = filtered.drop(offset).take(limit).map { it.toDomain() }
            val totalPages = (filtered.size + limit - 1) / limit
            Result.success(
                ShipmentsResult(
                    shipments = paginated,
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

    override suspend fun createShipment(
        startDate: String,
        endDate: String?,
        status: String
    ): Result<Shipment> {
        return try {
            val sDate = try { dbDateFormat.parse(startDate) ?: Date() } catch (e: Exception) { Date() }
            val eDate = endDate?.let { try { dbDateFormat.parse(it) } catch (e: Exception) { null } }

            val localShipment = ShipmentEntity(
                startDate = sDate,
                endDate = eDate,
                status = status,
                amountPerShipment = 0.0,
                syncState = SyncState.PENDING_CREATE
            )
            val localId = shipmentDao.insertShipment(localShipment).toInt()
            Result.success(shipmentDao.getShipmentById(localId)?.toDomain() ?: localShipment.copy(localId = localId).toDomain())
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
                val sDate = startDate?.let { try { dbDateFormat.parse(it) } catch (e: Exception) { null } } ?: existing.startDate
                val eDate = if (endDate == "") null else endDate?.let { try { dbDateFormat.parse(it) } catch (e: Exception) { null } } ?: existing.endDate

                val updatedLocal = existing.copy(
                    startDate = sDate,
                    endDate = eDate,
                    status = status ?: existing.status,
                    syncState = if (existing.remoteId == null) SyncState.PENDING_CREATE else SyncState.PENDING_UPDATE,
                    syncError = null
                )
                shipmentDao.insertShipment(updatedLocal)
                Result.success(updatedLocal.toDomain())
            } else {
                Result.failure(Exception("Shipment not found locally"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
