package aimar.rojas.avmadmin.features.workers.data

import com.google.gson.annotations.SerializedName

data class WorkerDto(
    val id: Int,
    @SerializedName("full_name")
    val fullName: String,
    val dni: String? = null,
    val phone: String? = null,
    @SerializedName("is_active")
    val isActive: Boolean = true,
    val notes: String? = null,
    @SerializedName("created_at")
    val createdAt: String? = null,
    @SerializedName("updated_at")
    val updatedAt: String? = null
)

data class WorkersResponseDto(
    val workers: List<WorkerDto>,
    val total: Int
)

data class CreateWorkerRequest(
    @SerializedName("full_name")
    val fullName: String,
    val dni: String? = null,
    val phone: String? = null,
    val notes: String? = null
)

data class UpdateWorkerRequest(
    @SerializedName("full_name")
    val fullName: String? = null,
    val dni: String? = null,
    val phone: String? = null,
    @SerializedName("is_active")
    val isActive: Boolean? = null,
    val notes: String? = null
)

data class WorkerResponseDto(
    val message: String,
    val worker: WorkerDto
)
