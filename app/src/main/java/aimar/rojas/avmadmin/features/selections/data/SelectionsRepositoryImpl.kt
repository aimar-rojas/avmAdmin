package aimar.rojas.avmadmin.features.selections.data

import aimar.rojas.avmadmin.core.sync.SyncState
import aimar.rojas.avmadmin.features.selections.data.local.SelectionDao
import aimar.rojas.avmadmin.features.selections.domain.SelectionsRepository
import aimar.rojas.avmadmin.features.selections.domain.model.SelectionDetail
import javax.inject.Inject

class SelectionsRepositoryImpl @Inject constructor(
    private val dao: SelectionDao
) : SelectionsRepository {

    override suspend fun getSelections(
        tradeId: Int?,
        selectionTypeId: Int?
    ): Result<List<SelectionDetail>> {
        return try {
            if (tradeId == null) {
                Result.failure(Exception("Cannot fetch selections without tradeId"))
            } else {
                val selections = dao.getSelectionsByTradeId(tradeId)
                    .map { it.toDomain() }
                    .filter { selectionTypeId == null || it.selectionTypeId == selectionTypeId }
                Result.success(selections)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getLocalSelections(tradeId: Int): Result<List<SelectionDetail>> {
        return getSelections(tradeId, null)
    }

    override suspend fun saveSelectionLocal(selection: SelectionDetail) {
        val existing = if (selection.selectionByTradeId > 0) {
            dao.getSelectionWithUnitWeights(selection.selectionByTradeId)
        } else {
            null
        }

        val syncState = when {
            existing == null -> SyncState.PENDING_CREATE
            existing.selection.remoteId == null -> SyncState.PENDING_CREATE
            else -> SyncState.PENDING_UPDATE
        }

        val localSelection = selection.toEntity().copy(
            localId = if (selection.selectionByTradeId > 0) selection.selectionByTradeId else 0,
            remoteId = existing?.selection?.remoteId ?: selection.remoteId,
            tradeLocalId = selection.tradeId,
            syncState = syncState,
            syncError = null
        )

        val selectionLocalId = if (localSelection.localId == 0) {
            dao.insertSelection(localSelection).toInt()
        } else {
            dao.insertSelection(localSelection)
            localSelection.localId
        }

        val previousWeights = existing?.unitWeights?.associateBy { it.localId }.orEmpty()
        dao.deleteUnitWeightsBySelectionId(selectionLocalId)
        dao.insertUnitWeights(
            selection.unitWeights.map { unitWeight ->
                val previous = previousWeights[unitWeight.unitWeightId]
                unitWeight.toEntity(selectionLocalId).copy(
                    localId = if (unitWeight.unitWeightId > 0) unitWeight.unitWeightId else 0,
                    remoteId = previous?.remoteId ?: unitWeight.remoteId,
                    selectionLocalId = selectionLocalId
                )
            }
        )
    }

    override fun getPendingSyncTradeIds(): kotlinx.coroutines.flow.Flow<List<Int>> {
        return dao.getPendingSyncTradeIds()
    }
}
