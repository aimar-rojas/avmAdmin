package aimar.rojas.avmadmin.features.selections.domain

import aimar.rojas.avmadmin.features.selections.domain.model.SelectionDetail

interface SelectionsRepository {
    suspend fun getSelections(
        tradeId: Int? = null,
        selectionTypeId: Int? = null
    ): Result<List<SelectionDetail>>

    suspend fun saveSelectionLocal(selection: SelectionDetail)
    suspend fun getLocalSelections(tradeId: Int): Result<List<SelectionDetail>>
    fun getPendingSyncTradeIds(): kotlinx.coroutines.flow.Flow<List<Int>>
    suspend fun getPendingSyncTradeIdsList(): List<Int>
    suspend fun syncAllSelectionsForTrade(tradeId: Int): Result<Unit>
}
