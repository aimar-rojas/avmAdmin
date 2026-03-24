package aimar.rojas.avmadmin.features.parties.data

import aimar.rojas.avmadmin.core.workers.AvmSyncWorker
import aimar.rojas.avmadmin.data.local.SessionDataStore
import aimar.rojas.avmadmin.domain.model.Party
import aimar.rojas.avmadmin.features.parties.data.local.PartyDao
import aimar.rojas.avmadmin.features.parties.data.local.entities.PartyEntity
import aimar.rojas.avmadmin.features.parties.domain.PartiesRepository
import aimar.rojas.avmadmin.features.parties.domain.PartiesResult
import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import retrofit2.HttpException
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

class PartiesRepositoryImpl @Inject constructor(
    private val partiesApiService: PartiesApiService,
    private val partyDao: PartyDao,
    private val sessionDataStore: SessionDataStore,
    @ApplicationContext private val context: Context
) : PartiesRepository {

    override suspend fun getParties(
        partyRole: String?,
        firstName: String?,
        lastName: String?,
        dni: String?,
        ruc: String?,
        phone: String?
    ): Result<PartiesResult> {
        try {
            val pendingParties = partyDao.getPendingSyncParties()
            if (pendingParties.isEmpty()) {
                val count = partyDao.getPartyCount()
                val lastSync = if (count == 0) null else sessionDataStore.getLastPartySync()
                val response = partiesApiService.getParties(
                    partyRole = partyRole,
                    firstName = firstName,
                    lastName = lastName,
                    dni = dni,
                    ruc = ruc,
                    phone = phone,
                    updatedAfter = lastSync
                )

                if (response.isSuccessful && response.body() != null) {
                    val serverParties = response.body()!!.parties
                    if (serverParties.isNotEmpty()) {
                        val entities = serverParties.map {
                            PartyEntity(
                                partyId = it.partyId,
                                partyRole = it.partyRole,
                                aliasName = it.aliasName,
                                firstName = it.firstName ?: "",
                                lastName = it.lastName,
                                dni = it.dni,
                                ruc = it.ruc,
                                phone = it.phone,
                                isPendingSync = false,
                                syncOperation = null
                            )
                        }
                        partyDao.insertParties(entities)
                        
                        val currentTime = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC") }.format(Date())
                        sessionDataStore.saveLastPartySync(currentTime)
                    }
                }
            }
        } catch (e: Exception) {
            // Ignore network errors, fall back to DB
        }
        
        return fetchFromLocalDB(partyRole, firstName, lastName, dni, ruc, phone)
    }

    private suspend fun fetchFromLocalDB(
        partyRole: String?,
        firstName: String?,
        lastName: String?,
        dni: String?,
        ruc: String?,
        phone: String?
    ): Result<PartiesResult> {
        return try {
            val allLocalParties = partyDao.getParties().first()
            val filtered = allLocalParties.filter { party ->
                (partyRole == null || party.partyRole.equals(partyRole, ignoreCase = true)) &&
                (firstName == null || party.firstName.contains(firstName, ignoreCase = true)) &&
                (lastName == null || party.lastName?.contains(lastName, ignoreCase = true) == true) &&
                (dni == null || party.dni?.contains(dni) == true) &&
                (ruc == null || party.ruc?.contains(ruc) == true) &&
                (phone == null || party.phone?.contains(phone) == true)
            }
            val mapped = filtered.map { it.toDomain() }
            Result.success(PartiesResult(parties = mapped, total = mapped.size))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createParty(
        partyRole: String,
        aliasName: String?,
        firstName: String?,
        lastName: String?,
        dni: String?,
        ruc: String?,
        phone: String?
    ): Result<Party> {
        return try {
            val tempId = -(System.currentTimeMillis() % Int.MAX_VALUE).toInt()
            val localParty = PartyEntity(
                partyId = tempId,
                partyRole = partyRole,
                aliasName = aliasName,
                firstName = firstName ?: "",
                lastName = lastName,
                dni = dni,
                ruc = ruc,
                phone = phone,
                isPendingSync = true,
                syncOperation = "CREATE"
            )
            partyDao.insertParty(localParty)
            enqueueSyncWorker()
            
            Result.success(localParty.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateParty(
        id: Int,
        partyRole: String?,
        aliasName: String?,
        firstName: String?,
        lastName: String?,
        dni: String?,
        ruc: String?,
        phone: String?
    ): Result<Party> {
        return try {
            val existing = partyDao.getPartyById(id)
            if (existing != null) {
                val updatedLocal = existing.copy(
                    partyRole = partyRole ?: existing.partyRole,
                    aliasName = aliasName ?: existing.aliasName,
                    firstName = firstName ?: existing.firstName,
                    lastName = lastName ?: existing.lastName,
                    dni = dni ?: existing.dni,
                    ruc = ruc ?: existing.ruc,
                    phone = phone ?: existing.phone,
                    isPendingSync = true,
                    syncOperation = if (existing.partyId <= 0) "CREATE" else "UPDATE"
                )
                partyDao.insertParty(updatedLocal)
                enqueueSyncWorker()
                Result.success(updatedLocal.toDomain())
            } else {
                Result.failure(Exception("Party not found locally"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun enqueueSyncWorker() {
        val workRequest = OneTimeWorkRequestBuilder<AvmSyncWorker>()
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            "SyncWork",
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }
}

