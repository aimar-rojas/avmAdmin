package aimar.rojas.avmadmin.core.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "id_mappings")
data class IdMappingEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val entityType: String, // "PARTY", "SHIPMENT", "TRADE"
    val oldId: Int,         // The negative temp ID
    val newId: Int          // The positive real ID from Postgres
)
