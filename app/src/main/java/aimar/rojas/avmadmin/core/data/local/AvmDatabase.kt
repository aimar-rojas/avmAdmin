package aimar.rojas.avmadmin.core.data.local

import aimar.rojas.avmadmin.features.selections.data.local.SelectionDao
import aimar.rojas.avmadmin.features.selections.data.local.entities.SelectionEntity
import aimar.rojas.avmadmin.features.selections.data.local.entities.UnitWeightEntity

import aimar.rojas.avmadmin.features.parties.data.local.PartyDao
import aimar.rojas.avmadmin.features.parties.data.local.entities.PartyEntity
import aimar.rojas.avmadmin.features.shipments.data.local.ShipmentDao
import aimar.rojas.avmadmin.features.shipments.data.local.entities.ShipmentEntity
import aimar.rojas.avmadmin.features.trades.data.local.TradeDao
import aimar.rojas.avmadmin.features.trades.data.local.entities.TradeEntity

import aimar.rojas.avmadmin.core.data.local.dao.IdMappingDao
import aimar.rojas.avmadmin.core.data.local.entities.IdMappingEntity

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        SelectionEntity::class,
        UnitWeightEntity::class,
        PartyEntity::class,
        ShipmentEntity::class,
        TradeEntity::class,
        IdMappingEntity::class
    ],
    version = 6,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AvmDatabase : RoomDatabase() {
    abstract val selectionDao: SelectionDao
    abstract val partyDao: PartyDao
    abstract val shipmentDao: ShipmentDao
    abstract val tradeDao: TradeDao
    abstract val idMappingDao: IdMappingDao
}
