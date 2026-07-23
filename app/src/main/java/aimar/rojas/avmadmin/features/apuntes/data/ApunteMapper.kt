package aimar.rojas.avmadmin.features.apuntes.data

import aimar.rojas.avmadmin.core.sync.SyncState
import aimar.rojas.avmadmin.features.apuntes.data.local.entities.ApunteDetailEntity
import aimar.rojas.avmadmin.features.apuntes.data.local.entities.ApunteEntity
import aimar.rojas.avmadmin.features.apuntes.data.local.entities.ApunteWithDetails
import aimar.rojas.avmadmin.features.apuntes.domain.model.Apunte
import aimar.rojas.avmadmin.features.apuntes.domain.model.ApunteDetail

fun ApunteDto.toDomain(): Apunte {
    return Apunte(
        id = id,
        remoteId = id,
        userId = userId,
        authorName = user?.displayName(),
        recordDate = recordDate,
        observations = observations,
        details = details?.map { it.toDomain() } ?: emptyList(),
        syncState = SyncState.CLEAN
    )
}

fun ApunteDetailDto.toDomain(): ApunteDetail {
    return ApunteDetail(
        id = id,
        remoteId = id,
        selectionTypeId = selectionTypeId,
        jabaCount = jabaCount,
        isEnabled = isEnabled,
        selectionType = null // Backend doesnt populate selectionType object deeply for records in the current backend design, or if it does, we map it here. For simplicity, UI has it hardcoded.
    )
}

fun ApunteWithDetails.toDomain(): Apunte {
    return Apunte(
        id = apunte.localId,
        remoteId = apunte.remoteId,
        userId = apunte.userId,
        authorName = apunte.authorName,
        recordDate = apunte.recordDate,
        observations = apunte.observations,
        details = details.map { it.toDomain() },
        syncState = apunte.syncState,
        syncError = apunte.syncError
    )
}

fun ApunteDetailEntity.toDomain(): ApunteDetail {
    return ApunteDetail(
        id = localId,
        remoteId = remoteId,
        selectionTypeId = selectionTypeId,
        jabaCount = jabaCount,
        isEnabled = isEnabled,
        selectionType = null
    )
}

fun ApunteDto.toEntity(
    localId: Int,
    now: String,
    syncState: String = SyncState.CLEAN
): ApunteEntity {
    return ApunteEntity(
        localId = localId,
        remoteId = id,
        userId = userId,
        authorName = user?.displayName(),
        recordDate = recordDate,
        observations = observations,
        syncState = syncState,
        lastSyncedAt = now,
        serverUpdatedAt = null,
        syncError = null
    )
}

fun ApunteDetailDto.toEntity(apunteLocalId: Int): ApunteDetailEntity {
    return ApunteDetailEntity(
        localId = 0,
        remoteId = id,
        apunteLocalId = apunteLocalId,
        selectionTypeId = selectionTypeId,
        jabaCount = jabaCount,
        isEnabled = isEnabled
    )
}

fun ApunteDetail.toEntity(apunteLocalId: Int): ApunteDetailEntity {
    return ApunteDetailEntity(
        localId = id,
        remoteId = remoteId,
        apunteLocalId = apunteLocalId,
        selectionTypeId = selectionTypeId,
        jabaCount = jabaCount,
        isEnabled = isEnabled
    )
}

private fun ApunteUserDto.displayName(): String? {
    return username?.takeIf { it.isNotBlank() }
        ?: email?.substringBefore("@")?.takeIf { it.isNotBlank() }
}
