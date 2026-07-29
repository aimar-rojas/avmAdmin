package aimar.rojas.avmadmin.core.data.local

import aimar.rojas.avmadmin.features.apuntes.data.local.ApuntesDao
import aimar.rojas.avmadmin.features.apuntes.data.local.entities.ApunteDetailEntity
import aimar.rojas.avmadmin.features.apuntes.data.local.entities.ApunteEntity
import aimar.rojas.avmadmin.features.selections.data.local.SelectionDao
import aimar.rojas.avmadmin.features.selections.data.local.entities.SelectionEntity
import aimar.rojas.avmadmin.features.selections.data.local.entities.UnitWeightEntity

import aimar.rojas.avmadmin.features.parties.data.local.PartyDao
import aimar.rojas.avmadmin.features.parties.data.local.entities.PartyEntity
import aimar.rojas.avmadmin.features.shipments.data.local.ShipmentDao
import aimar.rojas.avmadmin.features.shipments.data.local.ShipmentExpenseDao
import aimar.rojas.avmadmin.features.shipments.data.local.ShipmentLaborDao
import aimar.rojas.avmadmin.features.shipments.data.local.entities.ShipmentEntity
import aimar.rojas.avmadmin.features.shipments.data.local.entities.ShipmentExpenseEntity
import aimar.rojas.avmadmin.features.shipments.data.local.entities.ShipmentLaborEntity
import aimar.rojas.avmadmin.features.trades.data.local.TradeDao
import aimar.rojas.avmadmin.features.trades.data.local.entities.TradeEntity
import aimar.rojas.avmadmin.features.workers.data.local.WorkerDao
import aimar.rojas.avmadmin.features.workers.data.local.entities.WorkerEntity

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        SelectionEntity::class,
        UnitWeightEntity::class,
        PartyEntity::class,
        WorkerEntity::class,
        ShipmentEntity::class,
        ShipmentExpenseEntity::class,
        ShipmentLaborEntity::class,
        TradeEntity::class,
        ApunteEntity::class,
        ApunteDetailEntity::class
    ],
    version = 11,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AvmDatabase : RoomDatabase() {
    abstract val selectionDao: SelectionDao
    abstract val partyDao: PartyDao
    abstract val workerDao: WorkerDao
    abstract val shipmentDao: ShipmentDao
    abstract val shipmentExpenseDao: ShipmentExpenseDao
    abstract val shipmentLaborDao: ShipmentLaborDao
    abstract val tradeDao: TradeDao
    abstract val apuntesDao: ApuntesDao
}
