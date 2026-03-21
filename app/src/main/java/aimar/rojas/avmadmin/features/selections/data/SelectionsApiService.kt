package aimar.rojas.avmadmin.features.selections.data

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

import retrofit2.http.POST

interface SelectionsApiService {
    @GET("v1/selections")
    suspend fun getSelections(
        @Query("trade_id") tradeId: Int? = null,
        @Query("selection_type_id") selectionTypeId: Int? = null
    ): Response<SelectionsResponseDto>

    @POST("v1/selections")
    suspend fun createSelection(
        @Body request: UpdateSelectionRequestDto
    ): Response<SelectionUpdateResponseDto>

    @PUT("v1/selections/{id}")
    suspend fun updateSelection(
        @Path("id") id: Int,
        @Body request: UpdateSelectionRequestDto
    ): Response<SelectionUpdateResponseDto>
}
