package aimar.rojas.avmadmin.data.remote.api

import aimar.rojas.avmadmin.data.remote.dto.AuthResponseDto
import aimar.rojas.avmadmin.data.remote.dto.RegisterResponseDto
import aimar.rojas.avmadmin.features.login.data.LoginRequest
import aimar.rojas.avmadmin.features.register.data.RegisterRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {
    @POST("v1/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponseDto>

    @POST("v1/register")
    suspend fun register(@Body request: RegisterRequest): Response<RegisterResponseDto>
}
