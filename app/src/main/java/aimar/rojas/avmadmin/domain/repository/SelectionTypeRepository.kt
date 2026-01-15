package aimar.rojas.avmadmin.domain.repository

import aimar.rojas.avmadmin.domain.model.SelectionType

interface SelectionTypeRepository {
    suspend fun getSelections(): List<SelectionType>
    suspend fun addSelectionType(selectionType: SelectionType)
    suspend fun editSelectionType(selectionType: SelectionType)
}