package aimar.rojas.avmadmin.features.parties.data

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface PartiesApiService {

    @GET("v1/parties")
    suspend fun getParties(
        @Query("party_role") partyRole: String? = null,
        @Query("first_name") firstName: String? = null,
        @Query("last_name") lastName: String? = null,
        @Query("dni") dni: String? = null,
        @Query("ruc") ruc: String? = null,
        @Query("phone") phone: String? = null
    ): Response<PartiesResponseDto>

    @POST("v1/parties")
    suspend fun createParty(
        @Body request: CreatePartyRequest
    ): Response<CreatePartyResponseDto>

    @PUT("v1/parties/{id}")
    suspend fun updateParty(
        @Path("id") id: Int,
        @Body request: UpdatePartyRequest
    ): Response<UpdatePartyResponseDto>
}

