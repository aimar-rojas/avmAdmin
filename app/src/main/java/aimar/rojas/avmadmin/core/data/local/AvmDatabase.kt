package aimar.rojas.avmadmin.core.data.local

import aimar.rojas.avmadmin.features.selections.data.local.SelectionDao
import aimar.rojas.avmadmin.features.selections.data.local.entities.SelectionEntity
import aimar.rojas.avmadmin.features.selections.data.local.entities.UnitWeightEntity
import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        SelectionEntity::class,
        UnitWeightEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AvmDatabase : RoomDatabase() {
    abstract val selectionDao: SelectionDao
}
