package aimar.rojas.avmadmin.domain.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Trade(
    @SerializedName("trade_id")
    val tradeId: Int,
    @SerializedName("party_id")
    val partyId: Int,
    @SerializedName("boss_id")
    val bossId: Int,
    @SerializedName("shipment_id")
    val shipmentId: Int,
    @SerializedName("trade_type")
    val tradeType: String,
    @SerializedName("start_datetime")
    val startDatetime: String,
    @SerializedName("end_datetime")
    val endDatetime: String,
    @SerializedName("discount_weight_per_tray")
    val discountWeightPerTray: Double,
    @SerializedName("amount_per_trade")
    val amountPerTrade: Double
) : Parcelable