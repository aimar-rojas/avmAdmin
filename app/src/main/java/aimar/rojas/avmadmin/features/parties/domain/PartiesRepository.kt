package aimar.rojas.avmadmin.features.parties.domain

import aimar.rojas.avmadmin.domain.model.Party

interface PartiesRepository {

    suspend fun getParties(
        partyRole: String? = null,
        firstName: String? = null,
        lastName: String? = null,
        dni: String? = null,
        ruc: String? = null,
        phone: String? = null
    ): Result<PartiesResult>

    suspend fun createParty(
        partyRole: String,
        aliasName: String? = null,
        firstName: String? = null,
        lastName: String? = null,
        dni: String? = null,
        ruc: String? = null,
        phone: String? = null
    ): Result<Party>

    suspend fun updateParty(
        id: Int,
        partyRole: String? = null,
        aliasName: String? = null,
        firstName: String? = null,
        lastName: String? = null,
        dni: String? = null,
        ruc: String? = null,
        phone: String? = null
    ): Result<Party>
}

data class PartiesResult(
    val parties: List<Party>,
    val total: Int
)

