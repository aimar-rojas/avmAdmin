package aimar.rojas.avmadmin.features.shipments.data

import aimar.rojas.avmadmin.data.remote.dto.CreateShipmentRequest
import aimar.rojas.avmadmin.data.remote.dto.UpdateShipmentRequest
import aimar.rojas.avmadmin.data.remote.mapper.toDomain
import aimar.rojas.avmadmin.domain.model.Shipment
import aimar.rojas.avmadmin.features.shipments.domain.ShipmentsRepository
import aimar.rojas.avmadmin.features.shipments.domain.ShipmentsResult
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class ShipmentsRepositoryImpl @Inject constructor(
    private val shipmentsApiService: ShipmentsApiService
) : ShipmentsRepository {

    override suspend fun getShipments(
        page: Int,
        limit: Int,
        status: String?,
        startDate: String?,
        endDate: String?
    ): Result<ShipmentsResult> {
        return try {
            val response = shipmentsApiService.getShipments(
                page = page,
                limit = limit,
                status = status,
                startDate = startDate,
                endDate = endDate
            )
            
            if (response.isSuccessful && response.body() != null) {
                val responseDto = response.body()!!
                val shipments = responseDto.shipments.map { it.toDomain() }
                val result = ShipmentsResult(
                    shipments = shipments,
                    total = responseDto.total,
                    page = responseDto.page,
                    limit = responseDto.limit,
                    totalPages = responseDto.totalPages,
                    hasNext = responseDto.hasNext,
                    hasPrevious = responseDto.hasPrevious
                )
                Result.success(result)
            } else {
                val errorMessage = response.errorBody()?.string() ?: "Error desconocido"
                Result.failure(Exception(errorMessage))
            }
        } catch (e: HttpException) {
            Result.failure(Exception("Error de servidor: ${e.code()}"))
        } catch (e: IOException) {
            Result.failure(Exception("Error de conexión: ${e.message}"))
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
            val request = CreateShipmentRequest(
                startDate = startDate,
                endDate = endDate,
                status = status
            )
            val response = shipmentsApiService.createShipment(request)
            
            if (response.isSuccessful && response.body() != null) {
                val shipmentDto = response.body()!!.shipment
                val shipment = shipmentDto.toDomain()
                Result.success(shipment)
            } else {
                val errorMessage = response.errorBody()?.string() ?: "Error desconocido"
                Result.failure(Exception(errorMessage))
            }
        } catch (e: HttpException) {
            Result.failure(Exception("Error de servidor: ${e.code()}"))
        } catch (e: IOException) {
            Result.failure(Exception("Error de conexión: ${e.message}"))
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
            val request = UpdateShipmentRequest(
                startDate = startDate,
                endDate = endDate,
                status = status
            )
            val response = shipmentsApiService.updateShipment(id, request)
            
            if (response.isSuccessful && response.body() != null) {
                val shipmentDto = response.body()!!.shipment
                val shipment = shipmentDto.toDomain()
                Result.success(shipment)
            } else {
                val errorMessage = response.errorBody()?.string() ?: "Error desconocido"
                Result.failure(Exception(errorMessage))
            }
        } catch (e: HttpException) {
            Result.failure(Exception("Error de servidor: ${e.code()}"))
        } catch (e: IOException) {
            Result.failure(Exception("Error de conexión: ${e.message}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
