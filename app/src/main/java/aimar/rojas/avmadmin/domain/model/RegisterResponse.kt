package aimar.rojas.avmadmin.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class RegisterResponse(
    val message: String,
    val user: User
) : Parcelable
