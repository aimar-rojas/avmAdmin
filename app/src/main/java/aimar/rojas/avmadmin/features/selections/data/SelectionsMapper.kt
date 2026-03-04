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
        selectionTypeName = this.selectionType?.nameSelection
    )
}

fun UnitWeightDto.toDomain(): UnitWeightDetail {
    return UnitWeightDetail(
        unitWeightId = this.unitWeightId,
        weight = this.weight.toDoubleOrNull() ?: 0.0,
        amount = this.amount
    )
}
