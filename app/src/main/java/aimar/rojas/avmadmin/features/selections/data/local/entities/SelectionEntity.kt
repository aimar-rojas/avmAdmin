package aimar.rojas.avmadmin.features.selections.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "selections")
data class SelectionEntity(
    @PrimaryKey
    val selectionByTradeId: Int, // Can be used as local ID if not synced yet, or we could auto generate, but typically we want it to match backend.
    val tradeId: Int,
    val selectionTypeId: Int,
    val price: Double?,
    val selectionTypeName: String?,
    val isPendingSync: Boolean = false
)
