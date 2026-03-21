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
    @Query("SELECT * FROM selections WHERE tradeId = :tradeId")
    suspend fun getSelectionsByTradeId(tradeId: Int): List<SelectionWithUnitWeights>

    @Transaction
    @Query("SELECT * FROM selections WHERE selectionByTradeId = :selectionId")
    suspend fun getSelectionWithUnitWeights(selectionId: Int): SelectionWithUnitWeights?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSelection(selection: SelectionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSelections(selections: List<SelectionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUnitWeight(unitWeight: UnitWeightEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUnitWeights(unitWeights: List<UnitWeightEntity>)

    @Query("DELETE FROM unit_weights WHERE selectionByTradeId = :selectionId")
    suspend fun deleteUnitWeightsBySelectionId(selectionId: Int)

    @Transaction
    suspend fun saveSelectionWithUnitWeights(selectionData: SelectionWithUnitWeights) {
        insertSelection(selectionData.selection)
        deleteUnitWeightsBySelectionId(selectionData.selection.selectionByTradeId)
        insertUnitWeights(selectionData.unitWeights)
    }

    @Query("DELETE FROM selections WHERE selectionByTradeId = :selectionId")
    suspend fun deleteSelectionById(selectionId: Int)

    @Query("DELETE FROM selections WHERE tradeId = :tradeId")
    suspend fun deleteSelectionsByTradeId(tradeId: Int)

    @Query("SELECT DISTINCT tradeId FROM selections WHERE isPendingSync = 1")
    fun getPendingSyncTradeIds(): kotlinx.coroutines.flow.Flow<List<Int>>

    @Transaction
    @Query("SELECT * FROM selections WHERE tradeId = :tradeId AND isPendingSync = 1")
    suspend fun getPendingSelectionsByTradeId(tradeId: Int): List<SelectionWithUnitWeights>

    @Query("UPDATE selections SET isPendingSync = 0 WHERE tradeId = :tradeId")
    suspend fun markTradeSelectionsAsSynced(tradeId: Int)
}
