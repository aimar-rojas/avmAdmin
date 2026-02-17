package aimar.rojas.avmadmin.features.parties.data

import com.google.gson.annotations.SerializedName

data class PartyDto(
    @SerializedName("party_id")
    val partyId: Int,
    @SerializedName("party_role")
    val partyRole: String,
    @SerializedName("alias_name")
    val aliasName: String? = null,
    @SerializedName("first_name")
    val firstName: String? = null,
    @SerializedName("last_name")
    val lastName: String? = null,
    val dni: String? = null,
    val ruc: String? = null,
    val phone: String? = null,
    @SerializedName("created_at")
    val createdAt: String? = null,
    @SerializedName("updated_at")
    val updatedAt: String? = null
)

data class PartiesResponseDto(
    val parties: List<PartyDto>,
    val total: Int
)

data class CreatePartyRequest(
    @SerializedName("party_role")
    val partyRole: String,
    @SerializedName("alias_name")
    val aliasName: String? = null,
    @SerializedName("first_name")
    val firstName: String? = null,
    @SerializedName("last_name")
    val lastName: String? = null,
    val dni: String? = null,
    val ruc: String? = null,
    val phone: String? = null
)

data class UpdatePartyRequest(
    @SerializedName("party_role")
    val partyRole: String? = null,
    @SerializedName("alias_name")
    val aliasName: String? = null,
    @SerializedName("first_name")
    val firstName: String? = null,
    @SerializedName("last_name")
    val lastName: String? = null,
    val dni: String? = null,
    val ruc: String? = null,
    val phone: String? = null
)

data class CreatePartyResponseDto(
    val message: String,
    val party: PartyDto
)

data class UpdatePartyResponseDto(
    val message: String,
    val party: PartyDto
)

