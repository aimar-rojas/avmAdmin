package aimar.rojas.avmadmin.data.repository

import aimar.rojas.avmadmin.core.auth.TokenDataStore
import aimar.rojas.avmadmin.data.local.SessionDataStore
import aimar.rojas.avmadmin.data.remote.api.AuthApiService
import aimar.rojas.avmadmin.data.remote.mapper.toDomain
import aimar.rojas.avmadmin.domain.model.AuthResponse
import aimar.rojas.avmadmin.domain.model.RegisterResponse
import aimar.rojas.avmadmin.domain.repository.AuthRepository
import aimar.rojas.avmadmin.features.login.data.LoginRequest
import aimar.rojas.avmadmin.features.register.data.RegisterRequest
import kotlinx.coroutines.runBlocking
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authApiService: AuthApiService,
    private val tokenDataStore: TokenDataStore,
    private val sessionDataStore: SessionDataStore
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<AuthResponse> {
        return try {
            val request = LoginRequest(email = email, password = password)
            val response = authApiService.login(request)
            
            if (response.isSuccessful && response.body() != null) {
                val authResponseDto = response.body()!!

                val authResponse = authResponseDto.toDomain()

                tokenDataStore.saveToken(authResponse.token)
                sessionDataStore.saveUser(authResponse.user)
                sessionDataStore.saveIsCompletedProfile(authResponse.isCompletedProfile)
                Result.success(authResponse)
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

    override suspend fun register(
        username: String,
        email: String,
        password: String
    ): Result<RegisterResponse> {
        return try {
            val request = RegisterRequest(
                username = username,
                email = email,
                password = password
            )
            val response = authApiService.register(request)
            
            if (response.isSuccessful && response.body() != null) {
                val registerResponseDto = response.body()!!
                val registerResponse = registerResponseDto.toDomain()
                Result.success(registerResponse)
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

    override suspend fun isLoggedIn(): Boolean {
        return sessionDataStore.isLoggedIn()
    }

    override suspend fun logout() {
        sessionDataStore.clearSession()
    }

    override suspend fun getCurrentToken(): String? {
        return sessionDataStore.getToken()
    }
}
