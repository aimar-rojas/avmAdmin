package aimar.rojas.avmadmin.features.parties.data.local.entities

import aimar.rojas.avmadmin.core.sync.SyncState
import aimar.rojas.avmadmin.domain.model.Party
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "parties",
    indices = [Index(value = ["remoteId"], unique = true)]
)
data class PartyEntity(
    @PrimaryKey(autoGenerate = true)
    val localId: Int = 0,
    val remoteId: Int? = null,
    val partyRole: String,
    val aliasName: String?,
    val firstName: String,
    val lastName: String?,
    val dni: String?,
    val ruc: String?,
    val phone: String?,
    val accountNumber: String?,
    val syncState: String = SyncState.CLEAN,
    val lastSyncAttemptAt: String? = null,
    val lastSyncedAt: String? = null,
    val serverUpdatedAt: String? = null,
    val syncError: String? = null
) {
    fun toDomain(): Party {
        return Party(
            partyId = localId,
            remoteId = remoteId,
            partyRole = partyRole,
            aliasName = aliasName,
            firstName = firstName,
            lastName = lastName,
            dni = dni,
            ruc = ruc,
            phone = phone,
            accountNumber = accountNumber,
            syncState = syncState
        )
    }
}

fun Party.toEntity(syncState: String = SyncState.CLEAN): PartyEntity {
    return PartyEntity(
        localId = this.partyId,
        remoteId = this.remoteId,
        partyRole = this.partyRole,
        aliasName = this.aliasName,
        firstName = this.firstName,
        lastName = this.lastName,
        dni = this.dni,
        ruc = this.ruc,
        phone = this.phone,
        accountNumber = this.accountNumber,
        syncState = syncState
    )
}
