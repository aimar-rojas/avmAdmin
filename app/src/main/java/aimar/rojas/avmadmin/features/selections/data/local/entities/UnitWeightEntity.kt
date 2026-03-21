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
            parentColumns = ["selectionByTradeId"],
            childColumns = ["selectionByTradeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("selectionByTradeId")]
)
data class UnitWeightEntity(
    @PrimaryKey(autoGenerate = true)
    val unitWeightId: Int = 0,
    val selectionByTradeId: Int, // Changed to val to be immutable, wait, let's keep it straight
    val weight: Double,
    val amount: Int
)
