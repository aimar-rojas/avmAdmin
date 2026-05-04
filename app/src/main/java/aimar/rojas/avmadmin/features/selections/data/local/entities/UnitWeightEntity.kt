package aimar.rojas.avmadmin.features.selections.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "unit_weights",
    foreignKeys = [
        ForeignKey(
            entity = SelectionEntity::class,
            parentColumns = ["localId"],
            childColumns = ["selectionLocalId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("selectionLocalId"), Index(value = ["remoteId"], unique = true)]
)
data class UnitWeightEntity(
    @PrimaryKey(autoGenerate = true)
    val localId: Int = 0,
    val remoteId: Int? = null,
    val selectionLocalId: Int,
    val weight: Double,
    val amount: Int
)
