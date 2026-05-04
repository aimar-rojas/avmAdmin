package aimar.rojas.avmadmin.features.selections.data

import aimar.rojas.avmadmin.features.selections.domain.model.SelectionDetail
import aimar.rojas.avmadmin.features.selections.domain.model.UnitWeightDetail

fun SelectionByTradeDto.toDomain(): SelectionDetail {
    return SelectionDetail(
        selectionByTradeId = this.selectionByTradeId,
        remoteId = this.selectionByTradeId,
        tradeId = this.tradeId,
        selectionTypeId = this.selectionTypeId,
        price = this.price?.toDoubleOrNull(),
        unitWeights = this.unitWeights?.map { it.toDomain() } ?: emptyList(),
        selectionTypeName = this.selectionType?.nameSelection,
        isPendingSync = false,
        syncState = aimar.rojas.avmadmin.core.sync.SyncState.CLEAN
    )
}

fun UnitWeightDto.toDomain(): UnitWeightDetail {
    return UnitWeightDetail(
        unitWeightId = this.unitWeightId,
        remoteId = this.unitWeightId,
        weight = this.weight.toDoubleOrNull() ?: 0.0,
        amount = this.amount
    )
}

// Room to Domain
fun aimar.rojas.avmadmin.features.selections.data.local.entities.SelectionWithUnitWeights.toDomain(): SelectionDetail {
    return SelectionDetail(
        selectionByTradeId = this.selection.localId,
        remoteId = this.selection.remoteId,
        tradeId = this.selection.tradeLocalId,
        selectionTypeId = this.selection.selectionTypeId,
        price = this.selection.price,
        unitWeights = this.unitWeights.map { it.toDomain() },
        selectionTypeName = this.selection.selectionTypeName,
        isPendingSync = this.selection.syncState != aimar.rojas.avmadmin.core.sync.SyncState.CLEAN,
        syncState = this.selection.syncState
    )
}

fun aimar.rojas.avmadmin.features.selections.data.local.entities.UnitWeightEntity.toDomain(): UnitWeightDetail {
    return UnitWeightDetail(
        unitWeightId = this.localId,
        remoteId = this.remoteId,
        weight = this.weight,
        amount = this.amount
    )
}

// Domain to Room
fun SelectionDetail.toEntity(): aimar.rojas.avmadmin.features.selections.data.local.entities.SelectionEntity {
    return aimar.rojas.avmadmin.features.selections.data.local.entities.SelectionEntity(
        localId = this.selectionByTradeId,
        remoteId = this.remoteId,
        tradeLocalId = this.tradeId,
        selectionTypeId = this.selectionTypeId,
        price = this.price,
        selectionTypeName = this.selectionTypeName,
        syncState = this.syncState ?: if (this.isPendingSync) aimar.rojas.avmadmin.core.sync.SyncState.PENDING_UPDATE else aimar.rojas.avmadmin.core.sync.SyncState.CLEAN
    )
}

fun UnitWeightDetail.toEntity(selectionLocalId: Int): aimar.rojas.avmadmin.features.selections.data.local.entities.UnitWeightEntity {
    return aimar.rojas.avmadmin.features.selections.data.local.entities.UnitWeightEntity(
        localId = this.unitWeightId,
        remoteId = this.remoteId,
        selectionLocalId = selectionLocalId,
        weight = this.weight,
        amount = this.amount
    )
}

// Domain to Request DTO
fun SelectionDetail.toUpdateDto(): UpdateSelectionRequestDto {
    return UpdateSelectionRequestDto(
        tradeId = this.tradeId,
        selectionTypeId = this.selectionTypeId,
        price = this.price?.toString(),
        unitWeights = this.unitWeights.map {
            UnitWeightRequestDto(
                weight = it.weight.toString(),
                amount = it.amount
            )
        }
    )
}
