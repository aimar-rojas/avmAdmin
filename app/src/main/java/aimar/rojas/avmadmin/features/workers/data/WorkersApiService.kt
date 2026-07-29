package aimar.rojas.avmadmin.features.workers.data

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface WorkersApiService {
    @GET("v1/workers")
    suspend fun getWorkers(
        @Query("active_only") activeOnly: Boolean? = null,
        @Query("updated_after") updatedAfter: String? = null
    ): Response<WorkersResponseDto>

    @POST("v1/workers")
    suspend fun createWorker(
        @Body request: CreateWorkerRequest
    ): Response<WorkerResponseDto>

    @PUT("v1/workers/{id}")
    suspend fun updateWorker(
        @Path("id") id: Int,
        @Body request: UpdateWorkerRequest
    ): Response<WorkerResponseDto>
}
