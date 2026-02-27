package aimar.rojas.avmadmin.features.trades.data

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface TradesApiService {
    @GET("v1/trades")
    suspend fun getTrades(
        @Query("shipment_id") shipmentId: Int,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 50,
        @Query("trade_type") tradeType: String? = null
    ): Response<TradesResponseDto>

    @POST("v1/trades")
    suspend fun createTrade(
        @Body request: CreateTradeRequest
    ): Response<CreateTradeResponseDto>
}
