package aimar.rojas.avmadmin.features.apuntes.domain.model

import aimar.rojas.avmadmin.domain.model.SelectionType

data class Apunte(
    val id: Int,
    val remoteId: Int? = null,
    val userId: Int,
    val recordDate: String,
    val observations: String?,
    val details: List<ApunteDetail>,
    val syncState: String = aimar.rojas.avmadmin.core.sync.SyncState.CLEAN,
    val syncError: String? = null
)

data class ApunteDetail(
    val id: Int,
    val remoteId: Int? = null,
    val selectionTypeId: Int,
    val jabaCount: Int,
    val isEnabled: Boolean,
    val selectionType: SelectionType?
)
