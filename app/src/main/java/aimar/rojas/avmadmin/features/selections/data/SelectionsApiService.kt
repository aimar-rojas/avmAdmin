package aimar.rojas.avmadmin.features.selections.data

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface SelectionsApiService {
    @GET("v1/selections")
    suspend fun getSelections(
        @Query("trade_id") tradeId: Int? = null,
        @Query("selection_type_id") selectionTypeId: Int? = null
    ): Response<SelectionsResponseDto>
}
