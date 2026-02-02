package aimar.rojas.avmadmin.features.shipments.data

import retrofit2.Response
import retrofit2.http.*

interface ShipmentsApiService {
    @GET("v1/shipments")
    suspend fun getShipments(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 50,
        @Query("status") status: String? = null,
        @Query("start_date") startDate: String? = null,
        @Query("end_date") endDate: String? = null
    ): Response<ShipmentsResponseDto>

    @POST("v1/shipments")
    suspend fun createShipment(
        @Body request: CreateShipmentRequest
    ): Response<CreateShipmentResponseDto>

    @PUT("v1/shipments/{id}")
    suspend fun updateShipment(
        @Path("id") id: Int,
        @Body request: UpdateShipmentRequest
    ): Response<UpdateShipmentResponseDto>
}
