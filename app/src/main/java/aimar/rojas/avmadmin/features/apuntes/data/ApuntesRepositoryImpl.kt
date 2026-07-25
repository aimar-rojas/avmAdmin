package aimar.rojas.avmadmin.features.apuntes.data

import aimar.rojas.avmadmin.core.sync.SyncState
import aimar.rojas.avmadmin.data.local.SessionDataStore
import aimar.rojas.avmadmin.features.apuntes.data.local.ApuntesDao
import aimar.rojas.avmadmin.features.apuntes.data.local.entities.ApunteEntity
import aimar.rojas.avmadmin.features.apuntes.domain.ApuntesRepository
import aimar.rojas.avmadmin.features.apuntes.domain.model.Apunte
import aimar.rojas.avmadmin.features.apuntes.domain.model.ApunteDetail
import aimar.rojas.avmadmin.utils.DateUtils
import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class ApuntesRepositoryImpl @Inject constructor(
    private val apiService: ApuntesApiService,
    private val apuntesDao: ApuntesDao,
    private val sessionDataStore: SessionDataStore
) : ApuntesRepository {

    override fun observeApuntes(): Flow<List<Apunte>> {
        return combine(
            apuntesDao.observeApuntesWithDetails(),
            sessionDataStore.userFlow
        ) { records, user ->
            records
                .filter { record ->
                    record.apunte.userId == user?.id ||
                        (record.apunte.remoteId == null && record.apunte.userId == 0)
                }
                .map { it.toDomain() }
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
                        if (existing == null || existing.syncState == SyncState.CLEAN) {
                            apuntesDao.replaceRecordWithDetails(
                                dto.toEntity(localId = existing?.localId ?: 0, now = now),
                                dto.details.orEmpty().map { it.toEntity(existing?.localId ?: 0) }
                            )
                        }
                    }
                    Result.success(getOwnLocalApuntes())
                } else {
                    val localRecords = getOwnLocalApuntes()
                    if (localRecords.isNotEmpty()) {
                        Log.w(TAG, "Remote apuntes history returned an empty body; showing local records.")
                        Result.success(localRecords)
                    } else {
                        Result.failure(Exception("Cuerpo de respuesta vacío"))
                    }
                }
            } else {
                val error = response.errorBody()?.string()
                val localRecords = getOwnLocalApuntes()
                Log.w(TAG, "Remote apuntes history failed. http=${response.code()}, error=$error, localCount=${localRecords.size}")
                if (localRecords.isNotEmpty()) {
                    Result.success(localRecords)
                } else {
                    Result.failure(Exception("Error en red: ${response.code()}"))
                }
            }
        } catch (e: Exception) {
            val localRecords = getOwnLocalApuntes()
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
        val currentUser = sessionDataStore.getUser()
        val isLegacyLocalRecord = record.apunte.remoteId == null && record.apunte.userId == 0
        if (!isLegacyLocalRecord && record.apunte.userId != currentUser?.id) {
            return Result.failure(Exception("Apunte no encontrado"))
        }
        return Result.success(record.toDomain())
    }

    override suspend fun createApunte(
        observations: String,
        details: List<ApunteDetail>
    ): Result<Apunte> {
        val now = DateUtils.currentUtcSyncTimestamp()
        val currentUser = sessionDataStore.getUser()
        val localId = apuntesDao.insertRecordWithDetails(
            ApunteEntity(
                userId = currentUser?.id ?: 0,
                authorName = currentUser?.username,
                recordDate = now,
                observations = observations,
                syncState = SyncState.PENDING_CREATE
            ),
            details.map { it.toEntity(apunteLocalId = 0) }
        )
        val localRecord = apuntesDao.getApunteWithDetailsById(localId)
            ?: return Result.failure(Exception("No se pudo guardar el apunte localmente"))
        return Result.success(localRecord.toDomain())
    }

    override suspend fun updateApunte(
        localId: Int,
        observations: String,
        details: List<ApunteDetail>
    ): Result<Apunte> {
        val existing = apuntesDao.getApunteWithDetailsById(localId)
            ?: return Result.failure(Exception("Apunte no encontrado"))
        val currentUser = sessionDataStore.getUser()
        val isLegacyLocalRecord = existing.apunte.remoteId == null && existing.apunte.userId == 0
        if (!isLegacyLocalRecord && currentUser?.id != existing.apunte.userId) {
            return Result.failure(Exception("Solo el autor puede editar este apunte"))
        }
        val pendingState = if (existing.apunte.remoteId == null) {
            SyncState.PENDING_CREATE
        } else {
            SyncState.PENDING_UPDATE
        }
        apuntesDao.replaceRecordWithDetails(
            existing.apunte.copy(
                observations = observations,
                syncState = pendingState,
                lastSyncAttemptAt = null,
                syncError = null
            ),
            details.map { it.toEntity(apunteLocalId = localId) }
        )

        val updatedRecord = apuntesDao.getApunteWithDetailsById(localId)
            ?: return Result.failure(Exception("No se pudo actualizar el apunte localmente"))
        return Result.success(updatedRecord.toDomain())
    }

    private suspend fun getOwnLocalApuntes(): List<Apunte> {
        val currentUser = sessionDataStore.getUser()
        return apuntesDao.getApuntesWithDetails()
            .filter { record ->
                record.apunte.userId == currentUser?.id ||
                    (record.apunte.remoteId == null && record.apunte.userId == 0)
            }
            .map { it.toDomain() }
    }

    companion object {
        private const val TAG = "ApuntesRepository"
    }
}
