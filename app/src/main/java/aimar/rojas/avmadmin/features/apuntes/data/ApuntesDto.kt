package aimar.rojas.avmadmin.features.apuntes.data

import com.google.gson.annotations.SerializedName

data class ApuntesResponseDto(
    @SerializedName("records") val records: List<ApunteDto>
)

data class CreateApunteResponseDto(
    @SerializedName("message") val message: String,
    @SerializedName("record") val record: ApunteDto
)

data class ApunteDto(
    @SerializedName("selection_record_id") val id: Int,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("record_date") val recordDate: String,
    @SerializedName("observations") val observations: String?,
    @SerializedName("details") val details: List<ApunteDetailDto>?,
    @SerializedName("user") val user: ApunteUserDto? = null
)

data class ApunteUserDto(
    @SerializedName("id") val id: Int,
    @SerializedName("username") val username: String?,
    @SerializedName("email") val email: String?
)

data class ApunteDetailDto(
    @SerializedName("selection_record_detail_id") val id: Int,
    @SerializedName("selection_type_id") val selectionTypeId: Int,
    @SerializedName("jaba_count") val jabaCount: Int,
    @SerializedName("is_enabled") val isEnabled: Boolean
)

data class CreateApunteRequestDto(
    @SerializedName("observations") val observations: String,
    @SerializedName("details") val details: List<CreateApunteDetailRequestDto>
)

data class CreateApunteDetailRequestDto(
    @SerializedName("selection_type_id") val selectionTypeId: Int,
    @SerializedName("jaba_count") val jabaCount: Int,
    @SerializedName("is_enabled") val isEnabled: Boolean
)
