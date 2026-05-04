package aimar.rojas.avmadmin.features.parties.data

import aimar.rojas.avmadmin.core.sync.SyncState
import aimar.rojas.avmadmin.domain.model.Party
import aimar.rojas.avmadmin.features.parties.data.local.PartyDao
import aimar.rojas.avmadmin.features.parties.data.local.entities.PartyEntity
import aimar.rojas.avmadmin.features.parties.domain.PartiesRepository
import aimar.rojas.avmadmin.features.parties.domain.PartiesResult
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class PartiesRepositoryImpl @Inject constructor(
    private val partyDao: PartyDao
) : PartiesRepository {

    override suspend fun getParties(
        partyRole: String?,
        firstName: String?,
        lastName: String?,
        dni: String?,
        ruc: String?,
        phone: String?,
        accountNumber: String?
    ): Result<PartiesResult> {
        return try {
            val allLocalParties = partyDao.getParties().first()
            val filtered = allLocalParties.filter { party ->
                (partyRole == null || party.partyRole.equals(partyRole, ignoreCase = true)) &&
                    (firstName == null || party.firstName.contains(firstName, ignoreCase = true)) &&
                    (lastName == null || party.lastName?.contains(lastName, ignoreCase = true) == true) &&
                    (dni == null || party.dni?.contains(dni) == true) &&
                    (ruc == null || party.ruc?.contains(ruc) == true) &&
                    (phone == null || party.phone?.contains(phone) == true) &&
                    (accountNumber == null || party.accountNumber?.contains(accountNumber) == true)
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
        phone: String?,
        accountNumber: String?
    ): Result<Party> {
        return try {
            val localParty = PartyEntity(
                partyRole = partyRole,
                aliasName = aliasName,
                firstName = firstName ?: "",
                lastName = lastName,
                dni = dni,
                ruc = ruc,
                phone = phone,
                accountNumber = accountNumber,
                syncState = SyncState.PENDING_CREATE
            )
            val localId = partyDao.insertParty(localParty).toInt()
            Result.success(partyDao.getPartyById(localId)?.toDomain() ?: localParty.copy(localId = localId).toDomain())
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
        phone: String?,
        accountNumber: String?
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
                    accountNumber = accountNumber ?: existing.accountNumber,
                    syncState = if (existing.remoteId == null) SyncState.PENDING_CREATE else SyncState.PENDING_UPDATE,
                    syncError = null
                )
                partyDao.insertParty(updatedLocal)
                Result.success(updatedLocal.toDomain())
            } else {
                Result.failure(Exception("Party not found locally"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
