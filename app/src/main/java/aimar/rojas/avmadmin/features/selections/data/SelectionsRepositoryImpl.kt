package aimar.rojas.avmadmin.features.selections.data

import aimar.rojas.avmadmin.features.selections.domain.SelectionsRepository
import aimar.rojas.avmadmin.features.selections.domain.model.SelectionDetail
import javax.inject.Inject

class SelectionsRepositoryImpl @Inject constructor(
    private val api: SelectionsApiService
) : SelectionsRepository {

    override suspend fun getSelections(
        tradeId: Int?,
        selectionTypeId: Int?
    ): Result<List<SelectionDetail>> {
        return try {
            val response = api.getSelections(tradeId = tradeId, selectionTypeId = selectionTypeId)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.success(body.selections.map { it.toDomain() })
                } else {
                    Result.failure(Exception("Empty body response"))
                }
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Unknown error"
                Result.failure(Exception("Error ${response.code()}: $errorMsg"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
