package aimar.rojas.avmadmin.domain.repository

import aimar.rojas.avmadmin.domain.model.AuthResponse
import aimar.rojas.avmadmin.domain.model.RegisterResponse

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<AuthResponse>
    suspend fun register(username: String, email: String, password: String): Result<RegisterResponse>
    fun logout()
    fun isLoggedIn(): Boolean
    fun getCurrentToken(): String?
}
