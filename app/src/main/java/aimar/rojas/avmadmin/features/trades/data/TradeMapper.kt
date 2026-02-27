package aimar.rojas.avmadmin.features.trades.data

import aimar.rojas.avmadmin.domain.model.Trade

fun TradeDto.toDomain(): Trade {
    return Trade(
        tradeId = tradeId,
        partyId = partyId,
        bossId = bossId,
        shipmentId = shipmentId,
        tradeType = tradeType,
        startDatetime = startDatetime,
        endDatetime = endDatetime ?: "",
        discountWeightPerTray = discountWeightPerTray,
        amountPerTrade = amountPerTrade
    )
}
