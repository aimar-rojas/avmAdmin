package aimar.rojas.avmadmin.features.selections.data.local.entities

import androidx.room.Embedded
import androidx.room.Relation

data class SelectionWithUnitWeights(
    @Embedded val selection: SelectionEntity,
    @Relation(
        parentColumn = "localId",
        entityColumn = "selectionLocalId"
    )
    val unitWeights: List<UnitWeightEntity>
)
