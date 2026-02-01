package aimar.rojas.avmadmin.features.shipments.domain

import aimar.rojas.avmadmin.domain.model.Shipment

interface ShipmentsRepository {
    suspend fun getShipments(
        page: Int = 1,
        limit: Int = 50,
        status: String? = null,
        startDate: String? = null,
        endDate: String? = null
    ): Result<ShipmentsResult>
    
    suspend fun createShipment(
        startDate: String,
        endDate: String? = null,
        status: String
    ): Result<Shipment>
    
    suspend fun updateShipment(
        id: Int,
        startDate: String? = null,
        endDate: String? = null,
        status: String? = null
    ): Result<Shipment>
}

data class ShipmentsResult(
    val shipments: List<Shipment>,
    val total: Long,
    val page: Int,
    val limit: Int,
    val totalPages: Int,
    val hasNext: Boolean,
    val hasPrevious: Boolean
)