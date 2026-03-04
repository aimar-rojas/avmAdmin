package aimar.rojas.avmadmin.features.selections.domain.model

data class SelectionDetail(
    val selectionByTradeId: Int,
    val tradeId: Int,
    val selectionTypeId: Int,
    val price: Double?,
    val unitWeights: List<UnitWeightDetail>,
    val selectionTypeName: String?
)

data class UnitWeightDetail(
    val unitWeightId: Int,
    val weight: Double,
    val amount: Int
)
