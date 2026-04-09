package aimar.rojas.avmadmin.features.parties.data.local.entities

import aimar.rojas.avmadmin.domain.model.Party
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "parties")
data class PartyEntity(
    @PrimaryKey
    val partyId: Int,
    val partyRole: String,
    val aliasName: String?,
    val firstName: String,
    val lastName: String?,
    val dni: String?,
    val ruc: String?,
    val phone: String?,
    val accountNumber: String?,
    val isPendingSync: Boolean = false,
    val syncOperation: String? = null // CREATE, UPDATE, DELETE
) {
    fun toDomain(): Party {
        return Party(
            partyId = partyId,
            partyRole = partyRole,
            aliasName = aliasName,
            firstName = firstName,
            lastName = lastName,
            dni = dni,
            ruc = ruc,
            phone = phone,
            accountNumber = accountNumber
        )
    }
}

fun Party.toEntity(isPendingSync: Boolean = false, syncOperation: String? = null): PartyEntity {
    return PartyEntity(
        partyId = this.partyId,
        partyRole = this.partyRole,
        aliasName = this.aliasName,
        firstName = this.firstName,
        lastName = this.lastName,
        dni = this.dni,
        ruc = this.ruc,
        phone = this.phone,
        accountNumber = this.accountNumber,
        isPendingSync = isPendingSync,
        syncOperation = syncOperation
    )
}
