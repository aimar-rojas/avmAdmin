package aimar.rojas.avmadmin.features.trades.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import aimar.rojas.avmadmin.domain.model.Trade

@Entity(tableName = "trades")
data class TradeEntity(
    @PrimaryKey
    val tradeId: Int,
    val partyId: Int,
    val bossId: Int,
    val shipmentId: Int,
    val tradeType: String,
    val startDatetime: String,
    val endDatetime: String?,
    val discountWeightPerTray: Double,
    val varietyAvocado: String,
    val amountPerTrade: Double,
    
    val isPendingSync: Boolean = false,
    val syncOperation: String? = null
) {
    fun toDomain(): Trade {
        return Trade(
            tradeId = tradeId,
            partyId = partyId,
            bossId = bossId,
            shipmentId = shipmentId,
            tradeType = tradeType,
            startDatetime = startDatetime,
            endDatetime = endDatetime ?: "",
            discountWeightPerTray = discountWeightPerTray,
            varietyAvocado = varietyAvocado,
            amountPerTrade = amountPerTrade
        )
    }
}

fun Trade.toEntity(isPendingSync: Boolean = false, syncOperation: String? = null): TradeEntity {
    return TradeEntity(
        tradeId = this.tradeId,
        partyId = this.partyId,
        bossId = this.bossId,
        shipmentId = this.shipmentId,
        tradeType = this.tradeType,
        startDatetime = this.startDatetime,
        endDatetime = this.endDatetime,
        discountWeightPerTray = this.discountWeightPerTray,
        varietyAvocado = this.varietyAvocado,
        amountPerTrade = this.amountPerTrade,
        
        isPendingSync = isPendingSync,
        syncOperation = syncOperation
    )
}
