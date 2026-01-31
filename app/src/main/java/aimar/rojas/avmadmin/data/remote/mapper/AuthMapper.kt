package aimar.rojas.avmadmin.data.remote.mapper

import aimar.rojas.avmadmin.data.remote.dto.AuthResponseDto
import aimar.rojas.avmadmin.data.remote.dto.RegisterResponseDto
import aimar.rojas.avmadmin.data.remote.dto.UserDto
import aimar.rojas.avmadmin.domain.model.AuthResponse
import aimar.rojas.avmadmin.domain.model.RegisterResponse
import aimar.rojas.avmadmin.domain.model.User

fun UserDto.toDomain(): User {
    return User(
        id = id,
        username = username,
        email = email,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun AuthResponseDto.toDomain(): AuthResponse {
    return AuthResponse(
        token = token,
        user = user.toDomain(),
        isCompletedProfile = isCompletedProfile
    )
}

fun RegisterResponseDto.toDomain(): RegisterResponse {
    return RegisterResponse(
        message = message,
        user = user.toDomain()
    )
}
