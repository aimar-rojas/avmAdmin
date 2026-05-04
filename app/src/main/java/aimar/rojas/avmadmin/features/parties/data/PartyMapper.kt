package aimar.rojas.avmadmin.features.parties.data

import aimar.rojas.avmadmin.domain.model.Party

fun PartyDto.toDomain(): Party {
    return Party(
        partyId = partyId,
        remoteId = partyId,
        partyRole = partyRole,
        aliasName = aliasName,
        firstName = firstName ?: "",
        lastName = lastName,
        dni = dni,
        ruc = ruc,
        phone = phone,
        accountNumber = accountNumber
    )
}
