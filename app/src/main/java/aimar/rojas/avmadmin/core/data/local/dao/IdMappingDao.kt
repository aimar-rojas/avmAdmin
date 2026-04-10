package aimar.rojas.avmadmin.core.data.local.dao

import aimar.rojas.avmadmin.core.data.local.entities.IdMappingEntity
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface IdMappingDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMapping(mapping: IdMappingEntity)

    @Query("SELECT newId FROM id_mappings WHERE entityType = :entityType AND oldId = :oldId")
    suspend fun getNewId(entityType: String, oldId: Int): Int?

    @Query("DELETE FROM id_mappings WHERE oldId = :oldId")
    suspend fun deleteMapping(oldId: Int)
}
