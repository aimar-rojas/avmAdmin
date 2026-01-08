package aimar.rojas.avmadmin.domain.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Party(
    @SerializedName("party_id")
    val partyId: Int,
    @SerializedName("party_role")
    val partyRole: String,
    @SerializedName("alias_name")
    val aliasName: String? = null,
    @SerializedName("first_name")
    val firstName: String,
    @SerializedName("last_name")
    val lastName: String? = null,
    val dni: String? = null,
    val ruc: String? = null,
    val phone: String? = null
) : Parcelable