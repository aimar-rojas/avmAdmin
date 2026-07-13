package aimar.rojas.avmadmin.features.apuntes.domain.model

import aimar.rojas.avmadmin.domain.model.SelectionType

data class Apunte(
    val id: Int,
    val userId: Int,
    val recordDate: String,
    val observations: String?,
    val details: List<ApunteDetail>
)

data class ApunteDetail(
    val id: Int,
    val selectionTypeId: Int,
    val jabaCount: Int,
    val isEnabled: Boolean,
    val selectionType: SelectionType?
)
