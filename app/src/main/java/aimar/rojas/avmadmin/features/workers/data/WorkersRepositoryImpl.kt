package aimar.rojas.avmadmin.features.workers.data

import aimar.rojas.avmadmin.core.sync.SyncState
import aimar.rojas.avmadmin.features.workers.data.local.WorkerDao
import aimar.rojas.avmadmin.features.workers.data.local.entities.WorkerEntity
import aimar.rojas.avmadmin.features.workers.domain.WorkersRepository
import aimar.rojas.avmadmin.features.workers.domain.model.Worker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class WorkersRepositoryImpl @Inject constructor(
    private val workerDao: WorkerDao
) : WorkersRepository {

    override fun observeWorkers(activeOnly: Boolean): Flow<List<Worker>> {
        val source = if (activeOnly) workerDao.observeActiveWorkers() else workerDao.observeWorkers()
        return source.map { workers -> workers.map { it.toDomain() } }
    }

    override suspend fun createWorker(
        fullName: String,
        dni: String?,
        phone: String?,
        notes: String?
    ): Result<Worker> {
        return try {
            val worker = WorkerEntity(
                fullName = fullName.trim(),
                dni = dni?.trim(),
                phone = phone?.trim(),
                notes = notes?.trim(),
                syncState = SyncState.PENDING_CREATE
            )
            val localId = workerDao.insertWorker(worker).toInt()
            Result.success(worker.copy(localId = localId).toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateWorker(
        workerId: Int,
        fullName: String,
        dni: String?,
        phone: String?,
        isActive: Boolean,
        notes: String?
    ): Result<Worker> {
        return try {
            val existing = workerDao.getWorkerById(workerId)
                ?: return Result.failure(Exception("No se encontró el personal"))

            val updated = existing.copy(
                fullName = fullName.trim(),
                dni = dni?.trim(),
                phone = phone?.trim(),
                isActive = isActive,
                notes = notes?.trim(),
                syncState = if (existing.remoteId == null) SyncState.PENDING_CREATE else SyncState.PENDING_UPDATE,
                syncError = null
            )
            workerDao.insertWorker(updated)
            Result.success(updated.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deactivateWorker(workerId: Int): Result<Unit> {
        return try {
            val existing = workerDao.getWorkerById(workerId)
                ?: return Result.failure(Exception("No se encontró el personal"))

            if (existing.remoteId == null && existing.syncState == SyncState.PENDING_CREATE) {
                workerDao.deleteWorkerById(workerId)
            } else {
                workerDao.insertWorker(
                    existing.copy(
                        isActive = false,
                        syncState = SyncState.PENDING_UPDATE,
                        syncError = null
                    )
                )
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
