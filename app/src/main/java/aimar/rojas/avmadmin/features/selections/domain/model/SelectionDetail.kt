package aimar.rojas.avmadmin.features.selections.domain.model

data class SelectionDetail(
    val selectionByTradeId: Int,
    val remoteId: Int? = null,
    val tradeId: Int,
    val selectionTypeId: Int,
    val price: Double?,
    val unitWeights: List<UnitWeightDetail>,
    val selectionTypeName: String?,
    val isPendingSync: Boolean = false,
    val syncState: String? = null
)

data class UnitWeightDetail(
    val unitWeightId: Int,
    val remoteId: Int? = null,
    val weight: Double,
    val amount: Int
)
