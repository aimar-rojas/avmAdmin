package aimar.rojas.avmadmin.features.register.data

import com.google.gson.annotations.SerializedName

data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String,
    @SerializedName("registration_key")
    val registrationKey: String
)
