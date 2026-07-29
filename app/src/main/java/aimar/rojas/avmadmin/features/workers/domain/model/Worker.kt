package aimar.rojas.avmadmin.features.workers.domain.model

data class Worker(
    val workerId: Int,
    val remoteId: Int? = null,
    val fullName: String,
    val dni: String? = null,
    val phone: String? = null,
    val isActive: Boolean = true,
    val notes: String? = null,
    val syncState: String? = null,
    val syncError: String? = null
)
