package aimar.rojas.avmadmin.features.selections.data.local

import aimar.rojas.avmadmin.features.selections.data.local.entities.SelectionEntity
import aimar.rojas.avmadmin.features.selections.data.local.entities.SelectionWithUnitWeights
import aimar.rojas.avmadmin.features.selections.data.local.entities.UnitWeightEntity
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface SelectionDao {

    @Transaction
    @Query("SELECT * FROM selections WHERE tradeLocalId = :tradeId ORDER BY selectionTypeId ASC")
    suspend fun getSelectionsByTradeId(tradeId: Int): List<SelectionWithUnitWeights>

    @Transaction
    @Query("SELECT * FROM selections WHERE localId = :selectionId")
    suspend fun getSelectionWithUnitWeights(selectionId: Int): SelectionWithUnitWeights?

    @Transaction
    @Query("SELECT * FROM selections WHERE remoteId = :remoteId LIMIT 1")
    suspend fun getSelectionWithUnitWeightsByRemoteId(remoteId: Int): SelectionWithUnitWeights?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSelection(selection: SelectionEntity): Long

    @androidx.room.Update
    suspend fun updateSelection(selection: SelectionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSelections(selections: List<SelectionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUnitWeight(unitWeight: UnitWeightEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUnitWeights(unitWeights: List<UnitWeightEntity>)

    @Query("DELETE FROM unit_weights WHERE selectionLocalId = :selectionId")
    suspend fun deleteUnitWeightsBySelectionId(selectionId: Int)

    @Transaction
    suspend fun saveSelectionWithUnitWeights(selectionData: SelectionWithUnitWeights) {
        val selectionId = if (selectionData.selection.localId == 0) {
            insertSelection(selectionData.selection).toInt()
        } else {
            insertSelection(selectionData.selection)
            selectionData.selection.localId
        }
        deleteUnitWeightsBySelectionId(selectionId)
        insertUnitWeights(selectionData.unitWeights.map { it.copy(selectionLocalId = selectionId) })
    }

    @Query("DELETE FROM selections WHERE localId = :selectionId")
    suspend fun deleteSelectionById(selectionId: Int)

    @Query("DELETE FROM selections WHERE tradeLocalId = :tradeId")
    suspend fun deleteSelectionsByTradeId(tradeId: Int)

    @Query("SELECT COUNT(*) FROM selections WHERE syncState != 'CLEAN'")
    fun observePendingCount(): kotlinx.coroutines.flow.Flow<Int>

    @Transaction
    @Query("SELECT * FROM selections WHERE syncState != 'CLEAN' ORDER BY localId ASC")
    suspend fun getPendingSelections(): List<SelectionWithUnitWeights>

    @Transaction
    @Query("SELECT * FROM selections WHERE tradeLocalId = :tradeId AND syncState != 'CLEAN'")
    suspend fun getPendingSelectionsByTradeId(tradeId: Int): List<SelectionWithUnitWeights>

    @Query("SELECT DISTINCT tradeLocalId FROM selections WHERE syncState != 'CLEAN'")
    fun getPendingSyncTradeIds(): kotlinx.coroutines.flow.Flow<List<Int>>
}
