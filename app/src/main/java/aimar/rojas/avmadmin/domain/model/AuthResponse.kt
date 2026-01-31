package aimar.rojas.avmadmin.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AuthResponse(
    val token: String,
    val user: User,
    val isCompletedProfile: Boolean
) : Parcelable
