package aimar.rojas.avmadmin.features.apuntes.data

import aimar.rojas.avmadmin.core.sync.SyncState
import aimar.rojas.avmadmin.features.apuntes.data.local.ApuntesDao
import aimar.rojas.avmadmin.features.apuntes.data.local.entities.ApunteEntity
import aimar.rojas.avmadmin.features.apuntes.domain.ApuntesRepository
import aimar.rojas.avmadmin.features.apuntes.domain.model.Apunte
import aimar.rojas.avmadmin.features.apuntes.domain.model.ApunteDetail
import aimar.rojas.avmadmin.utils.DateUtils
import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ApuntesRepositoryImpl @Inject constructor(
    private val apiService: ApuntesApiService,
    private val apuntesDao: ApuntesDao
) : ApuntesRepository {

    override fun observeApuntes(): Flow<List<Apunte>> {
        return apuntesDao.observeApuntesWithDetails().map { records ->
            records.map { it.toDomain() }
        }
    }

    override suspend fun getApuntes(): Result<List<Apunte>> {
        return try {
            val response = apiService.getApuntes()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    val now = DateUtils.currentUtcSyncTimestamp()
                    body.records.forEach { dto ->
                        val existing = apuntesDao.getApunteByRemoteId(dto.id)
                        apuntesDao.replaceRecordWithDetails(
                            dto.toEntity(localId = existing?.localId ?: 0, now = now),
                            dto.details.orEmpty().map { it.toEntity(existing?.localId ?: 0) }
                        )
                    }
                    Result.success(apuntesDao.getApuntesWithDetails().map { it.toDomain() })
                } else {
                    val localRecords = apuntesDao.getApuntesWithDetails().map { it.toDomain() }
                    if (localRecords.isNotEmpty()) {
                        Log.w(TAG, "Remote apuntes history returned an empty body; showing local records.")
                        Result.success(localRecords)
                    } else {
                        Result.failure(Exception("Cuerpo de respuesta vacío"))
                    }
                }
            } else {
                val error = response.errorBody()?.string()
                val localRecords = apuntesDao.getApuntesWithDetails().map { it.toDomain() }
                Log.w(TAG, "Remote apuntes history failed. http=${response.code()}, error=$error, localCount=${localRecords.size}")
                if (localRecords.isNotEmpty()) {
                    Result.success(localRecords)
                } else {
                    Result.failure(Exception("Error en red: ${response.code()}"))
                }
            }
        } catch (e: Exception) {
            val localRecords = apuntesDao.getApuntesWithDetails().map { it.toDomain() }
            Log.e(TAG, "Remote apuntes history threw an exception; showing local records if available.", e)
            if (localRecords.isNotEmpty()) {
                Result.success(localRecords)
            } else {
                Result.failure(e)
            }
        }
    }

    override suspend fun getApunteById(localId: Int): Result<Apunte> {
        val record = apuntesDao.getApunteWithDetailsById(localId)
            ?: return Result.failure(Exception("Apunte no encontrado"))
        return Result.success(record.toDomain())
    }

    override suspend fun createApunte(
        observations: String,
        details: List<ApunteDetail>
    ): Result<Apunte> {
        val now = DateUtils.currentUtcSyncTimestamp()
        val requestDto = CreateApunteRequestDto(
            observations = observations,
            details = details.map {
                CreateApunteDetailRequestDto(
                    selectionTypeId = it.selectionTypeId,
                    jabaCount = it.jabaCount,
                    isEnabled = it.isEnabled
                )
            }
        )
        val localId = apuntesDao.insertRecordWithDetails(
            ApunteEntity(
                recordDate = now,
                observations = observations,
                syncState = SyncState.PENDING_CREATE
            ),
            details.map { it.toEntity(apunteLocalId = 0) }
        )
        val localRecord = apuntesDao.getApuntesWithDetails()
            .first { it.apunte.localId == localId }

        return try {
            val response = apiService.createApunte(requestDto)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    val remote = body.record
                    apuntesDao.replaceRecordWithDetails(
                        remote.toEntity(localId = localId, now = DateUtils.currentUtcSyncTimestamp()),
                        remote.details.orEmpty().map { it.toEntity(localId) }
                    )
                    Result.success(
                        apuntesDao.getApuntesWithDetails()
                            .first { it.apunte.localId == localId }
                            .toDomain()
                    )
                } else {
                    val message = "Cuerpo de respuesta vacío"
                    Log.w(TAG, "Create apunte succeeded without body. localId=$localId")
                    apuntesDao.insertApunte(localRecord.apunte.copy(syncState = SyncState.FAILED_CREATE, syncError = message))
                    Result.success(localRecord.toDomain())
                }
            } else {
                val error = response.errorBody()?.string() ?: "HTTP ${response.code()}"
                Log.w(TAG, "Create apunte failed. localId=$localId, http=${response.code()}, error=$error")
                apuntesDao.insertApunte(localRecord.apunte.copy(syncState = SyncState.FAILED_CREATE, syncError = error))
                Result.success(localRecord.toDomain())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Create apunte threw an exception. localId=$localId", e)
            apuntesDao.insertApunte(localRecord.apunte.copy(syncState = SyncState.FAILED_CREATE, syncError = e.message))
            Result.success(localRecord.toDomain())
        }
    }

    override suspend fun updateApunte(
        localId: Int,
        observations: String,
        details: List<ApunteDetail>
    ): Result<Apunte> {
        val existing = apuntesDao.getApunteWithDetailsById(localId)
            ?: return Result.failure(Exception("Apunte no encontrado"))
        val pendingState = if (existing.apunte.remoteId == null) {
            SyncState.PENDING_CREATE
        } else {
            SyncState.PENDING_UPDATE
        }
        val now = DateUtils.currentUtcSyncTimestamp()
        apuntesDao.replaceRecordWithDetails(
            existing.apunte.copy(
                observations = observations,
                syncState = pendingState,
                lastSyncAttemptAt = null,
                syncError = null
            ),
            details.map { it.toEntity(apunteLocalId = localId) }
        )

        val requestDto = details.toCreateRequest(observations)
        return try {
            val remoteId = existing.apunte.remoteId
            val response = if (remoteId == null) {
                apiService.createApunte(requestDto)
            } else {
                apiService.updateApunte(remoteId, requestDto)
            }
            if (response.isSuccessful && response.body() != null) {
                val remote = response.body()!!.record
                apuntesDao.replaceRecordWithDetails(
                    remote.toEntity(localId = localId, now = DateUtils.currentUtcSyncTimestamp()),
                    remote.details.orEmpty().map { it.toEntity(localId) }
                )
                Result.success(
                    apuntesDao.getApunteWithDetailsById(localId)?.toDomain()
                        ?: existing.toDomain()
                )
            } else {
                val error = response.errorBody()?.string() ?: "HTTP ${response.code()}"
                Log.w(TAG, "Update apunte failed. localId=$localId, remoteId=$remoteId, http=${response.code()}, error=$error")
                apuntesDao.insertApunte(
                    existing.apunte.copy(
                        observations = observations,
                        syncState = SyncState.failureStateFor(pendingState),
                        lastSyncAttemptAt = now,
                        syncError = error
                    )
                )
                Result.success(apuntesDao.getApunteWithDetailsById(localId)?.toDomain() ?: existing.toDomain())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Update apunte threw an exception. localId=$localId", e)
            apuntesDao.insertApunte(
                existing.apunte.copy(
                    observations = observations,
                    syncState = SyncState.failureStateFor(pendingState),
                    lastSyncAttemptAt = now,
                    syncError = e.message
                )
            )
            Result.success(apuntesDao.getApunteWithDetailsById(localId)?.toDomain() ?: existing.toDomain())
        }
    }

    private fun List<ApunteDetail>.toCreateRequest(observations: String): CreateApunteRequestDto {
        return CreateApunteRequestDto(
            observations = observations,
            details = map {
                CreateApunteDetailRequestDto(
                    selectionTypeId = it.selectionTypeId,
                    jabaCount = it.jabaCount,
                    isEnabled = it.isEnabled
                )
            }
        )
    }

    companion object {
        private const val TAG = "ApuntesRepository"
    }
}
