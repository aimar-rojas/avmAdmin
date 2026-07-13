package aimar.rojas.avmadmin.features.apuntes.data

import aimar.rojas.avmadmin.features.apuntes.domain.model.Apunte
import aimar.rojas.avmadmin.features.apuntes.domain.model.ApunteDetail

fun ApunteDto.toDomain(): Apunte {
    return Apunte(
        id = id,
        userId = userId,
        recordDate = recordDate,
        observations = observations,
        details = details?.map { it.toDomain() } ?: emptyList()
    )
}

fun ApunteDetailDto.toDomain(): ApunteDetail {
    return ApunteDetail(
        id = id,
        selectionTypeId = selectionTypeId,
        jabaCount = jabaCount,
        isEnabled = isEnabled,
        selectionType = null // Backend doesnt populate selectionType object deeply for records in the current backend design, or if it does, we map it here. For simplicity, UI has it hardcoded.
    )
}
