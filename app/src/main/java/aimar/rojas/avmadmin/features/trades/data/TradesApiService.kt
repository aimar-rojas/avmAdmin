package aimar.rojas.avmadmin.features.trades.data

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface TradesApiService {
    @GET("v1/trades")
    suspend fun getTrades(
        @Query("shipment_id") shipmentId: Int? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 50,
        @Query("trade_type") tradeType: String? = null,
        @Query("updated_after") updatedAfter: String? = null
    ): Response<TradesResponseDto>

    @POST("v1/trades")
    suspend fun createTrade(
        @Body request: CreateTradeRequest
    ): Response<CreateTradeResponseDto>

    @PUT("v1/trades/{id}")
    suspend fun updateTrade(
        @Path("id") id: Int,
        @Body request: UpdateTradeRequest
    ): Response<CreateTradeResponseDto>

    @DELETE("v1/trades/{id}")
    suspend fun deleteTrade(
        @Path("id") id: Int
    ): Response<DeleteTradeResponseDto>
}
