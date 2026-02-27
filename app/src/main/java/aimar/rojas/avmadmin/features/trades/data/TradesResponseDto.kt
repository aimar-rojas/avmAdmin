package aimar.rojas.avmadmin.features.trades.data

import aimar.rojas.avmadmin.features.parties.data.PartyDto
import aimar.rojas.avmadmin.features.shipments.data.ShipmentDto
import com.google.gson.annotations.SerializedName

data class TradeDto(
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
    val endDatetime: String? = null,
    @SerializedName("discount_weight_per_tray")
    val discountWeightPerTray: Double,
    @SerializedName("variety_avocado")
    val varietyAvocado: String? = null,
    @SerializedName("party")
    val party: PartyDto? = null,
    @SerializedName("boss")
    val boss: BossDto? = null,
    @SerializedName("shipment")
    val shipment: ShipmentDto? = null,
    @SerializedName("amount_per_trade")
    val amountPerTrade: Double = 0.0
)

data class BossDto(
    @SerializedName("boss_id")
    val bossId: Int,
    @SerializedName("name_boss")
    val nameBoss: String,
    @SerializedName("boss_type")
    val bossType: String,
    @SerializedName("user_id")
    val userId: Int
)

data class TradesResponseDto(
    val trades: List<TradeDto>,
    val total: Long,
    val page: Int,
    val limit: Int,
    @SerializedName("total_pages")
    val totalPages: Int,
    @SerializedName("has_next")
    val hasNext: Boolean,
    @SerializedName("has_previous")
    val hasPrevious: Boolean
)
