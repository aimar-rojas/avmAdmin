package aimar.rojas.avmadmin.features.parties.data

import aimar.rojas.avmadmin.domain.model.Party

fun PartyDto.toDomain(): Party {
    return Party(
        partyId = partyId,
        partyRole = partyRole,
        aliasName = aliasName,
        firstName = firstName ?: "",
        lastName = lastName,
        dni = dni,
        ruc = ruc,
        phone = phone
    )
}

