package aimar.rojas.avmadmin.features.workers.domain

import aimar.rojas.avmadmin.features.workers.domain.model.Worker
import kotlinx.coroutines.flow.Flow

interface WorkersRepository {
    fun observeWorkers(activeOnly: Boolean = false): Flow<List<Worker>>

    suspend fun createWorker(
        fullName: String,
        dni: String?,
        phone: String?,
        notes: String?
    ): Result<Worker>

    suspend fun updateWorker(
        workerId: Int,
        fullName: String,
        dni: String?,
        phone: String?,
        isActive: Boolean,
        notes: String?
    ): Result<Worker>

    suspend fun deactivateWorker(workerId: Int): Result<Unit>
}
