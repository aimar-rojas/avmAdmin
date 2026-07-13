package aimar.rojas.avmadmin.features.apuntes.data

import aimar.rojas.avmadmin.features.apuntes.domain.ApuntesRepository
import aimar.rojas.avmadmin.features.apuntes.domain.model.Apunte
import aimar.rojas.avmadmin.features.apuntes.domain.model.ApunteDetail
import javax.inject.Inject

class ApuntesRepositoryImpl @Inject constructor(
    private val apiService: ApuntesApiService
) : ApuntesRepository {

    override suspend fun getApuntes(): Result<List<Apunte>> {
        return try {
            val response = apiService.getApuntes()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.success(body.records.map { it.toDomain() })
                } else {
                    Result.failure(Exception("Cuerpo de respuesta vacío"))
                }
            } else {
                Result.failure(Exception("Error en red: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createApunte(
        observations: String,
        details: List<ApunteDetail>
    ): Result<Apunte> {
        return try {
            val requestDto = CreateApunteRequestDto(
                observations = observations,
                details = details.map {
                    CreateApunteDetailRequestDto(
                        selectionTypeId = it.selectionTypeId,
                        jabaCount = it.jabaCount,
                        isEnabled = it.isEnabled
                    )
                }
            )
            val response = apiService.createApunte(requestDto)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.success(body.record.toDomain())
                } else {
                    Result.failure(Exception("Cuerpo de respuesta vacío"))
                }
            } else {
                Result.failure(Exception("Error en red: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
