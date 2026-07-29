package aimar.rojas.avmadmin.features.shipments.data

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ShipmentLaborApiService {
    @GET("v1/shipment-labor")
    suspend fun getLabor(
        @Query("shipment_id") shipmentId: Int? = null,
        @Query("worker_id") workerId: Int? = null,
        @Query("updated_after") updatedAfter: String? = null
    ): Response<ShipmentLaborResponseDto>

    @POST("v1/shipment-labor")
    suspend fun createLabor(
        @Body request: CreateShipmentLaborRequest
    ): Response<ShipmentLaborItemResponseDto>

    @PUT("v1/shipment-labor/{id}")
    suspend fun updateLabor(
        @Path("id") id: Int,
        @Body request: UpdateShipmentLaborRequest
    ): Response<ShipmentLaborItemResponseDto>
}
