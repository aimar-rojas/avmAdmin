package aimar.rojas.avmadmin.data.remote.dto

import com.google.gson.annotations.SerializedName

data class RegisterResponseDto(
    val message: String,
    val user: UserDto
)
