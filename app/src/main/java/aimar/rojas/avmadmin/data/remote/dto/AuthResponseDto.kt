package aimar.rojas.avmadmin.data.remote.dto

import com.google.gson.annotations.SerializedName

data class AuthResponseDto(
    val token: String,
    val user: UserDto,
    @SerializedName("isCompletedProfile")
    val isCompletedProfile: Boolean
)

data class UserDto(
    val id: Int,
    val username: String,
    val email: String,
    @SerializedName("created_at")
    val createdAt: String? = null,
    @SerializedName("updated_at")
    val updatedAt: String? = null
)
