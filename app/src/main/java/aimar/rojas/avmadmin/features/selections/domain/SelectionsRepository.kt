package aimar.rojas.avmadmin.features.selections.domain

import aimar.rojas.avmadmin.features.selections.domain.model.SelectionDetail

interface SelectionsRepository {
    suspend fun getSelections(
        tradeId: Int? = null,
        selectionTypeId: Int? = null
    ): Result<List<SelectionDetail>>
}
