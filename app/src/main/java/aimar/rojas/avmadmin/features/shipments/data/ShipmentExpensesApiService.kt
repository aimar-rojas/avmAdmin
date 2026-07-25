package aimar.rojas.avmadmin.features.shipments.data

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ShipmentExpensesApiService {
    @GET("v1/shipment-expenses")
    suspend fun getExpenses(
        @Query("shipment_id") shipmentId: Int? = null,
        @Query("category") category: String? = null,
        @Query("updated_after") updatedAfter: String? = null
    ): Response<ShipmentExpensesResponseDto>

    @POST("v1/shipment-expenses")
    suspend fun createExpense(
        @Body request: CreateShipmentExpenseRequest
    ): Response<ShipmentExpenseResponseDto>

    @PUT("v1/shipment-expenses/{id}")
    suspend fun updateExpense(
        @Path("id") id: Int,
        @Body request: UpdateShipmentExpenseRequest
    ): Response<ShipmentExpenseResponseDto>
}
