package aimar.rojas.avmadmin.features.shipments.data.local.entities

import aimar.rojas.avmadmin.core.sync.SyncState
import aimar.rojas.avmadmin.features.shipments.domain.model.ShipmentExpense
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "shipment_expenses",
    foreignKeys = [
        ForeignKey(
            entity = ShipmentEntity::class,
            parentColumns = ["localId"],
            childColumns = ["shipmentLocalId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["remoteId"], unique = true),
        Index(value = ["shipmentLocalId"]),
        Index(value = ["category"]),
        Index(value = ["expenseDate"])
    ]
)
data class ShipmentExpenseEntity(
    @PrimaryKey(autoGenerate = true)
    val localId: Int = 0,
    val remoteId: Int? = null,
    val shipmentLocalId: Int,
    val category: String,
    val subcategory: String? = null,
    val amount: Double,
    val quantity: Double? = null,
    val unitPrice: Double? = null,
    val description: String? = null,
    val expenseDate: String,
    val paidByPartyLocalId: Int? = null,
    val syncState: String = SyncState.CLEAN,
    val lastSyncAttemptAt: String? = null,
    val lastSyncedAt: String? = null,
    val serverUpdatedAt: String? = null,
    val syncError: String? = null
) {
    fun toDomain(): ShipmentExpense {
        return ShipmentExpense(
            expenseId = localId,
            remoteId = remoteId,
            shipmentId = shipmentLocalId,
            category = category,
            subcategory = subcategory,
            amount = amount,
            quantity = quantity,
            unitPrice = unitPrice,
            description = description,
            expenseDate = expenseDate,
            paidByPartyId = paidByPartyLocalId,
            syncState = syncState,
            syncError = syncError
        )
    }
}
