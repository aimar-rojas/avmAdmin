package aimar.rojas.avmadmin.features.apuntes.data

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.POST

interface ApuntesApiService {
    @GET("v1/selection-records")
    suspend fun getApuntes(): Response<ApuntesResponseDto>

    @POST("v1/selection-records")
    suspend fun createApunte(@Body request: CreateApunteRequestDto): Response<CreateApunteResponseDto>

    @PUT("v1/selection-records/{id}")
    suspend fun updateApunte(
        @Path("id") id: Int,
        @Body request: CreateApunteRequestDto
    ): Response<CreateApunteResponseDto>
}
