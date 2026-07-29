package aimar.rojas.avmadmin.features.shipments.domain

import aimar.rojas.avmadmin.features.shipments.domain.model.ShipmentLabor
import kotlinx.coroutines.flow.Flow

interface ShipmentLaborRepository {
    fun observeLaborByShipment(shipmentId: Int): Flow<List<ShipmentLabor>>

    suspend fun createLabor(
        shipmentId: Int,
        workerId: Int,
        workDate: String,
        amount: Double,
        notes: String?
    ): Result<ShipmentLabor>
}
