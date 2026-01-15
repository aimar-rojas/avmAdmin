package aimar.rojas.avmadmin.domain.repository

import aimar.rojas.avmadmin.domain.model.Shipment

interface ShipmentRepository {
    suspend fun getShipments(): List<Shipment>
    suspend fun getShipmentById(id: Int): Shipment
    suspend fun addShipment(shipment: Shipment)
    suspend fun editShipment(shipment: Shipment)
}