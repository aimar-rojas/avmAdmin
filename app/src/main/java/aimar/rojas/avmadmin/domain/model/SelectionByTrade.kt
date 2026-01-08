package aimar.rojas.avmadmin.domain.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class SelectionByTrade(
    @SerializedName("selection_by_trade")
    val selectionByTrade: Int,
    @SerializedName("trade_id")
    val tradeId: Int,
    @SerializedName("selection_type_id")
    val selectionTypeId: Int,
    val price: Double
) : Parcelable