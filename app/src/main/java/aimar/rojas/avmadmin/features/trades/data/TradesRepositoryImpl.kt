package aimar.rojas.avmadmin.features.trades.data

import aimar.rojas.avmadmin.features.trades.domain.TradesRepository
import aimar.rojas.avmadmin.features.trades.domain.TradesResult
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class TradesRepositoryImpl @Inject constructor(
    private val tradesApiService: TradesApiService
) : TradesRepository {

    override suspend fun getTrades(
        shipmentId: Int,
        page: Int,
        limit: Int,
        tradeType: String?
    ): Result<TradesResult> {
        return try {
            val response = tradesApiService.getTrades(
                shipmentId = shipmentId,
                page = page,
                limit = limit,
                tradeType = tradeType
            )
            
            if (response.isSuccessful && response.body() != null) {
                val responseDto = response.body()!!
                val trades = responseDto.trades.map { it.toDomain() }
                val result = TradesResult(
                    trades = trades,
                    total = responseDto.total,
                    page = responseDto.page,
                    limit = responseDto.limit,
                    totalPages = responseDto.totalPages,
                    hasNext = responseDto.hasNext,
                    hasPrevious = responseDto.hasPrevious
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
}
