package aimar.rojas.avmadmin.features.parties.data

import aimar.rojas.avmadmin.domain.model.Party
import aimar.rojas.avmadmin.features.parties.domain.PartiesRepository
import aimar.rojas.avmadmin.features.parties.domain.PartiesResult
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class PartiesRepositoryImpl @Inject constructor(
    private val partiesApiService: PartiesApiService
) : PartiesRepository {

    override suspend fun getParties(
        partyRole: String?,
        firstName: String?,
        lastName: String?,
        dni: String?,
        ruc: String?,
        phone: String?
    ): Result<PartiesResult> {
        return try {
            val response = partiesApiService.getParties(
                partyRole = partyRole,
                firstName = firstName,
                lastName = lastName,
                dni = dni,
                ruc = ruc,
                phone = phone
            )

            if (response.isSuccessful && response.body() != null) {
                val responseDto = response.body()!!
                val parties: List<Party> = responseDto.parties.map { it.toDomain() }
                val result = PartiesResult(
                    parties = parties,
                    total = responseDto.total
                )
                Result.success(result)
            } else {
                val errorMessage = response.errorBody()?.string() ?: "Error desconocido"
                Result.failure(Exception(errorMessage))
            }
        } catch (e: HttpException) {
            Result.failure(Exception("Error de servidor: ${e.code()}"))
        } catch (e: IOException) {
            Result.failure(Exception("Error de conexión: ${e.message}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createParty(
        partyRole: String,
        aliasName: String?,
        firstName: String?,
        lastName: String?,
        dni: String?,
        ruc: String?,
        phone: String?
    ): Result<Party> {
        return try {
            val request = CreatePartyRequest(
                partyRole = partyRole,
                aliasName = aliasName,
                firstName = firstName,
                lastName = lastName,
                dni = dni,
                ruc = ruc,
                phone = phone
            )

            val response = partiesApiService.createParty(request)

            if (response.isSuccessful && response.body() != null) {
                val partyDto = response.body()!!.party
                val party = partyDto.toDomain()
                Result.success(party)
            } else {
                val errorMessage = response.errorBody()?.string() ?: "Error desconocido"
                Result.failure(Exception(errorMessage))
            }
        } catch (e: HttpException) {
            Result.failure(Exception("Error de servidor: ${e.code()}"))
        } catch (e: IOException) {
            Result.failure(Exception("Error de conexión: ${e.message}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateParty(
        id: Int,
        partyRole: String?,
        aliasName: String?,
        firstName: String?,
        lastName: String?,
        dni: String?,
        ruc: String?,
        phone: String?
    ): Result<Party> {
        return try {
            val request = UpdatePartyRequest(
                partyRole = partyRole,
                aliasName = aliasName,
                firstName = firstName,
                lastName = lastName,
                dni = dni,
                ruc = ruc,
                phone = phone
            )

            val response = partiesApiService.updateParty(id, request)

            if (response.isSuccessful && response.body() != null) {
                val partyDto = response.body()!!.party
                val party = partyDto.toDomain()
                Result.success(party)
            } else {
                val errorMessage = response.errorBody()?.string() ?: "Error desconocido"
                Result.failure(Exception(errorMessage))
            }
        } catch (e: HttpException) {
            Result.failure(Exception("Error de servidor: ${e.code()}"))
        } catch (e: IOException) {
            Result.failure(Exception("Error de conexión: ${e.message}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

