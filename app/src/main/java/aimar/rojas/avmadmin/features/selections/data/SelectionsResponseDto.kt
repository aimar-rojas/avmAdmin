package aimar.rojas.avmadmin.features.selections.data

import com.google.gson.annotations.SerializedName

data class SelectionsResponseDto(
    val selections: List<SelectionByTradeDto>,
    val total: Int
)

data class SelectionByTradeDto(
    @SerializedName("selection_by_trade_id")
    val selectionByTradeId: Int,
    @SerializedName("trade_id")
    val tradeId: Int,
    @SerializedName("selection_type_id")
    val selectionTypeId: Int,
    val price: String?,
    @SerializedName("unit_weights")
    val unitWeights: List<UnitWeightDto>? = emptyList(),
    val trade: TradeReferenceDto? = null,
    @SerializedName("selection_type")
    val selectionType: SelectionTypeReferenceDto? = null
)

data class UnitWeightDto(
    @SerializedName("unit_weight_id")
    val unitWeightId: Int,
    val weight: String,
    val amount: Int
)

data class TradeReferenceDto(
    @SerializedName("trade_id")
    val tradeId: Int,
    @SerializedName("trade_type")
    val tradeType: String
)

data class SelectionTypeReferenceDto(
    @SerializedName("selection_type_id")
    val selectionTypeId: Int,
    @SerializedName("name_selection")
    val nameSelection: String
)

data class UpdateSelectionRequestDto(
    @SerializedName("trade_id")
    val tradeId: Int,
    @SerializedName("selection_type_id")
    val selectionTypeId: Int,
    val price: String?,
    @SerializedName("unit_weights")
    val unitWeights: List<UnitWeightRequestDto>
)

data class UnitWeightRequestDto(
    val weight: String,
    val amount: Int
)

data class SelectionUpdateResponseDto(
    val message: String,
    val selection: SelectionByTradeDto
)
