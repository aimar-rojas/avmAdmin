package aimar.rojas.avmadmin.features.selections.data

import aimar.rojas.avmadmin.features.selections.domain.model.SelectionDetail
import aimar.rojas.avmadmin.features.selections.domain.model.UnitWeightDetail

fun SelectionByTradeDto.toDomain(): SelectionDetail {
    return SelectionDetail(
        selectionByTradeId = this.selectionByTradeId,
        tradeId = this.tradeId,
        selectionTypeId = this.selectionTypeId,
        price = this.price?.toDoubleOrNull(),
        unitWeights = this.unitWeights?.map { it.toDomain() } ?: emptyList(),
        selectionTypeName = this.selectionType?.nameSelection,
        isPendingSync = false // From API it's never pending sync initially
    )
}

fun UnitWeightDto.toDomain(): UnitWeightDetail {
    return UnitWeightDetail(
        unitWeightId = this.unitWeightId,
        weight = this.weight.toDoubleOrNull() ?: 0.0,
        amount = this.amount
    )
}

// Room to Domain
fun aimar.rojas.avmadmin.features.selections.data.local.entities.SelectionWithUnitWeights.toDomain(): SelectionDetail {
    return SelectionDetail(
        selectionByTradeId = this.selection.selectionByTradeId,
        tradeId = this.selection.tradeId,
        selectionTypeId = this.selection.selectionTypeId,
        price = this.selection.price,
        unitWeights = this.unitWeights.map { it.toDomain() },
        selectionTypeName = this.selection.selectionTypeName,
        isPendingSync = this.selection.isPendingSync
    )
}

fun aimar.rojas.avmadmin.features.selections.data.local.entities.UnitWeightEntity.toDomain(): UnitWeightDetail {
    return UnitWeightDetail(
        unitWeightId = this.unitWeightId,
        weight = this.weight,
        amount = this.amount
    )
}

// Domain to Room
fun SelectionDetail.toEntity(): aimar.rojas.avmadmin.features.selections.data.local.entities.SelectionEntity {
    return aimar.rojas.avmadmin.features.selections.data.local.entities.SelectionEntity(
        selectionByTradeId = this.selectionByTradeId,
        tradeId = this.tradeId,
        selectionTypeId = this.selectionTypeId,
        price = this.price,
        selectionTypeName = this.selectionTypeName,
        isPendingSync = this.isPendingSync
    )
}

fun UnitWeightDetail.toEntity(selectionByTradeId: Int): aimar.rojas.avmadmin.features.selections.data.local.entities.UnitWeightEntity {
    return aimar.rojas.avmadmin.features.selections.data.local.entities.UnitWeightEntity(
        unitWeightId = this.unitWeightId, // 0 if it's new
        selectionByTradeId = selectionByTradeId,
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
