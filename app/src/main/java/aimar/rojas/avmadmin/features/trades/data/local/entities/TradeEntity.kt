package aimar.rojas.avmadmin.features.trades.data.local.entities

import aimar.rojas.avmadmin.core.sync.SyncState
import aimar.rojas.avmadmin.domain.model.Trade
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "trades",
    indices = [
        Index(value = ["remoteId"], unique = true),
        Index(value = ["shipmentLocalId"]),
        Index(value = ["partyLocalId"])
    ]
)
data class TradeEntity(
    @PrimaryKey(autoGenerate = true)
    val localId: Int = 0,
    val remoteId: Int? = null,
    val partyLocalId: Int,
    val bossId: Int,
    val shipmentLocalId: Int,
    val tradeType: String,
    val startDatetime: String,
    val endDatetime: String?,
    val discountWeightPerTray: Double,
    val varietyAvocado: String,
    val amountPerTrade: Double,
    val syncState: String = SyncState.CLEAN,
    val lastSyncAttemptAt: String? = null,
    val lastSyncedAt: String? = null,
    val serverUpdatedAt: String? = null,
    val syncError: String? = null
) {
    fun toDomain(): Trade {
        return Trade(
            tradeId = localId,
            remoteId = remoteId,
            partyId = partyLocalId,
            bossId = bossId,
            shipmentId = shipmentLocalId,
            tradeType = tradeType,
            startDatetime = startDatetime,
            endDatetime = endDatetime ?: "",
            discountWeightPerTray = discountWeightPerTray,
            varietyAvocado = varietyAvocado,
            amountPerTrade = amountPerTrade,
            syncState = syncState
        )
    }
}

fun Trade.toEntity(syncState: String = SyncState.CLEAN): TradeEntity {
    return TradeEntity(
        localId = this.tradeId,
        remoteId = this.remoteId,
        partyLocalId = this.partyId,
        bossId = this.bossId,
        shipmentLocalId = this.shipmentId,
        tradeType = this.tradeType,
        startDatetime = this.startDatetime,
        endDatetime = this.endDatetime,
        discountWeightPerTray = this.discountWeightPerTray,
        varietyAvocado = this.varietyAvocado,
        amountPerTrade = this.amountPerTrade,
        syncState = syncState
    )
}
