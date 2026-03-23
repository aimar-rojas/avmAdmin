package aimar.rojas.avmadmin.features.selections.data

import aimar.rojas.avmadmin.features.selections.data.local.SelectionDao
import aimar.rojas.avmadmin.features.selections.data.local.entities.SelectionWithUnitWeights
import aimar.rojas.avmadmin.features.selections.domain.SelectionsRepository
import aimar.rojas.avmadmin.features.selections.domain.model.SelectionDetail
import javax.inject.Inject

class SelectionsRepositoryImpl @Inject constructor(
    private val api: SelectionsApiService,
    private val dao: SelectionDao
) : SelectionsRepository {

    override suspend fun getSelections(
        tradeId: Int?,
        selectionTypeId: Int?
    ): Result<List<SelectionDetail>> {
        return try {
            // First check if there are any pending local syncs for this trade
            if (tradeId != null) {
                val pendingSelections = dao.getPendingSelectionsByTradeId(tradeId)
                if (pendingSelections.isNotEmpty()) {
                    // We must serve local data to avoid overwriting unsynced modifications
                    return fetchFromLocalDB(tradeId)
                }
            }

            // If no local pending changes, try to fetch from API
            val response = api.getSelections(tradeId = tradeId, selectionTypeId = selectionTypeId)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    val domainSelections = body.selections.map { it.toDomain() }
                    
                    // Delete old local selections before replacing with new remote ones
                    if (tradeId != null) {
                        dao.deleteSelectionsByTradeId(tradeId)
                    }
                    
                    // Save to local database
                    domainSelections.forEach { selection ->
                        dao.saveSelectionWithUnitWeights(
                            SelectionWithUnitWeights(
                                selection = selection.toEntity(),
                                unitWeights = selection.unitWeights.map { it.toEntity(selection.selectionByTradeId) }
                            )
                        )
                    }
                    
                    Result.success(domainSelections)
                } else {
                    Result.failure(Exception("Empty body response"))
                }
            } else {
                // If network fails, try fetching from local DB
                fetchFromLocalDB(tradeId)
            }
        } catch (e: Exception) {
            // If exception (no network), try local DB
            fetchFromLocalDB(tradeId)
        }
    }

    override suspend fun getLocalSelections(tradeId: Int): Result<List<SelectionDetail>> {
        return fetchFromLocalDB(tradeId)
    }

    private suspend fun fetchFromLocalDB(tradeId: Int?): Result<List<SelectionDetail>> {
        return try {
            if (tradeId != null) {
                val localSelections = dao.getSelectionsByTradeId(tradeId)
                Result.success(localSelections.map { it.toDomain() })
            } else {
                // We don't have a get all query, usually we only fetch by tradeId
                Result.failure(Exception("Cannot fetch all from local DB without tradeId"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveSelectionLocal(selection: SelectionDetail) {
        dao.saveSelectionWithUnitWeights(
            SelectionWithUnitWeights(
                selection = selection.toEntity(),
                unitWeights = selection.unitWeights.map { it.toEntity(selection.selectionByTradeId) }
            )
        )
    }

    override fun getPendingSyncTradeIds(): kotlinx.coroutines.flow.Flow<List<Int>> {
        return dao.getPendingSyncTradeIds()
    }

    override suspend fun getPendingSyncTradeIdsList(): List<Int> {
        return dao.getPendingSyncTradeIdsList()
    }

    override suspend fun syncAllSelectionsForTrade(tradeId: Int): Result<Unit> {
        if (tradeId <= 0) {
            return Result.failure(Exception("El Negocio aún no se ha sincronizado (ID offline). Por favor espera la sincronización automática."))
        }
        return try {
            val pendingSelections = dao.getPendingSelectionsByTradeId(tradeId)
            
            if (pendingSelections.isEmpty()) {
                return Result.success(Unit) // Nothing to sync
            }
            
            var allSuccess = true
            var lastErrorMsg = ""

            for (localData in pendingSelections) {
                val domainData = localData.toDomain()
                val requestDto = domainData.toUpdateDto()
                
                // If ID is <= 0, it means it was created locally and never synced (needs POST)
                val response = if (domainData.selectionByTradeId <= 0) {
                    api.createSelection(requestDto)
                } else {
                    api.updateSelection(domainData.selectionByTradeId, requestDto)
                }
                
                if (!response.isSuccessful) {
                    allSuccess = false
                    lastErrorMsg = response.errorBody()?.string() ?: "Unknown error"
                    break
                }
            }

            if (allSuccess) {
                // If all selections for this trade synced successfully, clear the pending flag
                dao.markTradeSelectionsAsSynced(tradeId)
                Result.success(Unit)
            } else {
                Result.failure(Exception("Error syncing trade $tradeId: $lastErrorMsg"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
