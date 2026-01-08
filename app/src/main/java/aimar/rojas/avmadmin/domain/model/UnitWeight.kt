package aimar.rojas.avmadmin.domain.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class UnitWeight(
    @SerializedName("unit_weight")
    val unitWeight: Int,
    @SerializedName("selection_by_trade_id")
    val selectionByTradeId: Int,
    val weight: Double,
    val amount: Int
) : Parcelable