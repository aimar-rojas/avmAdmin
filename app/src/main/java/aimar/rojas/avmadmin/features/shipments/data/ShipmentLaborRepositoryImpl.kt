package aimar.rojas.avmadmin.features.shipments.data

import aimar.rojas.avmadmin.core.sync.SyncState
import aimar.rojas.avmadmin.features.shipments.data.local.ShipmentLaborDao
import aimar.rojas.avmadmin.features.shipments.data.local.entities.ShipmentLaborEntity
import aimar.rojas.avmadmin.features.shipments.domain.ShipmentLaborRepository
import aimar.rojas.avmadmin.features.shipments.domain.model.ShipmentLabor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ShipmentLaborRepositoryImpl @Inject constructor(
    private val laborDao: ShipmentLaborDao
) : ShipmentLaborRepository {

    override fun observeLaborByShipment(shipmentId: Int): Flow<List<ShipmentLabor>> {
        return laborDao.observeLaborByShipment(shipmentId)
            .map { rows -> rows.map { it.toDomain() } }
    }

    override suspend fun createLabor(
        shipmentId: Int,
        workerId: Int,
        workDate: String,
        amount: Double,
        notes: String?
    ): Result<ShipmentLabor> {
        return try {
            val existing = laborDao.getLaborByWorkerDay(shipmentId, workerId, workDate)
            val labor = ShipmentLaborEntity(
                localId = existing?.localId ?: 0,
                remoteId = existing?.remoteId,
                shipmentLocalId = shipmentId,
                workerLocalId = workerId,
                workDate = workDate,
                amount = amount,
                notes = notes?.trim(),
                syncState = if (existing?.remoteId == null) SyncState.PENDING_CREATE else SyncState.PENDING_UPDATE,
                serverUpdatedAt = existing?.serverUpdatedAt
            )
            val localId = laborDao.insertLabor(labor).toInt()
            Result.success(
                ShipmentLabor(
                    laborId = localId,
                    shipmentId = shipmentId,
                    workerId = workerId,
                    workerName = "",
                    workDate = workDate,
                    amount = amount,
                    notes = notes,
                    syncState = SyncState.PENDING_CREATE
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
