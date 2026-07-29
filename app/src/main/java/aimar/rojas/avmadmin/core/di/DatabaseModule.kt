package aimar.rojas.avmadmin.core.di

import aimar.rojas.avmadmin.core.data.local.AvmDatabase
import aimar.rojas.avmadmin.features.apuntes.data.local.ApuntesDao
import aimar.rojas.avmadmin.features.selections.data.local.SelectionDao
import aimar.rojas.avmadmin.features.parties.data.local.PartyDao
import aimar.rojas.avmadmin.features.shipments.data.local.ShipmentDao
import aimar.rojas.avmadmin.features.shipments.data.local.ShipmentExpenseDao
import aimar.rojas.avmadmin.features.shipments.data.local.ShipmentLaborDao
import aimar.rojas.avmadmin.features.trades.data.local.TradeDao
import aimar.rojas.avmadmin.features.workers.data.local.WorkerDao
import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE parties ADD COLUMN accountNumber TEXT")
        }
    }

    val MIGRATION_5_6 = object : Migration(5, 6) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `id_mappings` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `entityType` TEXT NOT NULL, `oldId` INTEGER NOT NULL, `newId` INTEGER NOT NULL)"
            )
        }
    }

    val MIGRATION_6_7 = object : Migration(6, 7) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `parties_new` (
                    `localId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `remoteId` INTEGER,
                    `partyRole` TEXT NOT NULL,
                    `aliasName` TEXT,
                    `firstName` TEXT NOT NULL,
                    `lastName` TEXT,
                    `dni` TEXT,
                    `ruc` TEXT,
                    `phone` TEXT,
                    `accountNumber` TEXT,
                    `syncState` TEXT NOT NULL,
                    `lastSyncAttemptAt` TEXT,
                    `lastSyncedAt` TEXT,
                    `serverUpdatedAt` TEXT,
                    `syncError` TEXT
                )
                """.trimIndent()
            )
            db.execSQL(
                """
                INSERT INTO `parties_new` (`localId`, `remoteId`, `partyRole`, `aliasName`, `firstName`, `lastName`, `dni`, `ruc`, `phone`, `accountNumber`, `syncState`)
                SELECT
                    `partyId`,
                    CASE WHEN `partyId` > 0 THEN `partyId` ELSE NULL END,
                    `partyRole`,
                    `aliasName`,
                    `firstName`,
                    `lastName`,
                    `dni`,
                    `ruc`,
                    `phone`,
                    `accountNumber`,
                    CASE
                        WHEN `isPendingSync` = 1 AND (`syncOperation` = 'CREATE' OR `partyId` <= 0) THEN 'PENDING_CREATE'
                        WHEN `isPendingSync` = 1 THEN 'PENDING_UPDATE'
                        ELSE 'CLEAN'
                    END
                FROM `parties`
                """.trimIndent()
            )

            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `shipments_new` (
                    `localId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `remoteId` INTEGER,
                    `startDate` INTEGER NOT NULL,
                    `endDate` INTEGER,
                    `status` TEXT NOT NULL,
                    `amountPerShipment` REAL NOT NULL,
                    `syncState` TEXT NOT NULL,
                    `lastSyncAttemptAt` TEXT,
                    `lastSyncedAt` TEXT,
                    `serverUpdatedAt` TEXT,
                    `syncError` TEXT
                )
                """.trimIndent()
            )
            db.execSQL(
                """
                INSERT INTO `shipments_new` (`localId`, `remoteId`, `startDate`, `endDate`, `status`, `amountPerShipment`, `syncState`)
                SELECT
                    `shipmentId`,
                    CASE WHEN `shipmentId` > 0 THEN `shipmentId` ELSE NULL END,
                    `startDate`,
                    `endDate`,
                    `status`,
                    `amountPerShipment`,
                    CASE
                        WHEN `isPendingSync` = 1 AND (`syncOperation` = 'CREATE' OR `shipmentId` <= 0) THEN 'PENDING_CREATE'
                        WHEN `isPendingSync` = 1 THEN 'PENDING_UPDATE'
                        ELSE 'CLEAN'
                    END
                FROM `shipments`
                """.trimIndent()
            )

            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `trades_new` (
                    `localId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `remoteId` INTEGER,
                    `partyLocalId` INTEGER NOT NULL,
                    `bossId` INTEGER NOT NULL,
                    `shipmentLocalId` INTEGER NOT NULL,
                    `tradeType` TEXT NOT NULL,
                    `startDatetime` TEXT NOT NULL,
                    `endDatetime` TEXT,
                    `discountWeightPerTray` REAL NOT NULL,
                    `varietyAvocado` TEXT NOT NULL,
                    `amountPerTrade` REAL NOT NULL,
                    `syncState` TEXT NOT NULL,
                    `lastSyncAttemptAt` TEXT,
                    `lastSyncedAt` TEXT,
                    `serverUpdatedAt` TEXT,
                    `syncError` TEXT
                )
                """.trimIndent()
            )
            db.execSQL(
                """
                INSERT INTO `trades_new` (`localId`, `remoteId`, `partyLocalId`, `bossId`, `shipmentLocalId`, `tradeType`, `startDatetime`, `endDatetime`, `discountWeightPerTray`, `varietyAvocado`, `amountPerTrade`, `syncState`)
                SELECT
                    `tradeId`,
                    CASE WHEN `tradeId` > 0 THEN `tradeId` ELSE NULL END,
                    `partyId`,
                    `bossId`,
                    `shipmentId`,
                    `tradeType`,
                    `startDatetime`,
                    `endDatetime`,
                    `discountWeightPerTray`,
                    `varietyAvocado`,
                    `amountPerTrade`,
                    CASE
                        WHEN `isPendingSync` = 1 AND (`syncOperation` = 'CREATE' OR `tradeId` <= 0) THEN 'PENDING_CREATE'
                        WHEN `isPendingSync` = 1 THEN 'PENDING_UPDATE'
                        ELSE 'CLEAN'
                    END
                FROM `trades`
                """.trimIndent()
            )

            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `selections_new` (
                    `localId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `remoteId` INTEGER,
                    `tradeLocalId` INTEGER NOT NULL,
                    `selectionTypeId` INTEGER NOT NULL,
                    `price` REAL,
                    `selectionTypeName` TEXT,
                    `syncState` TEXT NOT NULL,
                    `lastSyncAttemptAt` TEXT,
                    `lastSyncedAt` TEXT,
                    `serverUpdatedAt` TEXT,
                    `syncError` TEXT
                )
                """.trimIndent()
            )
            db.execSQL(
                """
                INSERT INTO `selections_new` (`localId`, `remoteId`, `tradeLocalId`, `selectionTypeId`, `price`, `selectionTypeName`, `syncState`)
                SELECT
                    `selectionByTradeId`,
                    CASE WHEN `selectionByTradeId` > 0 THEN `selectionByTradeId` ELSE NULL END,
                    `tradeId`,
                    `selectionTypeId`,
                    `price`,
                    `selectionTypeName`,
                    CASE
                        WHEN `isPendingSync` = 1 AND `selectionByTradeId` <= 0 THEN 'PENDING_CREATE'
                        WHEN `isPendingSync` = 1 THEN 'PENDING_UPDATE'
                        ELSE 'CLEAN'
                    END
                FROM `selections`
                """.trimIndent()
            )

            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `unit_weights_new` (
                    `localId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `remoteId` INTEGER,
                    `selectionLocalId` INTEGER NOT NULL,
                    `weight` REAL NOT NULL,
                    `amount` INTEGER NOT NULL,
                    FOREIGN KEY(`selectionLocalId`) REFERENCES `selections`(`localId`) ON UPDATE NO ACTION ON DELETE CASCADE
                )
                """.trimIndent()
            )
            db.execSQL(
                """
                INSERT INTO `unit_weights_new` (`localId`, `remoteId`, `selectionLocalId`, `weight`, `amount`)
                SELECT
                    `unitWeightId`,
                    CASE WHEN `unitWeightId` > 0 THEN `unitWeightId` ELSE NULL END,
                    `selectionByTradeId`,
                    `weight`,
                    `amount`
                FROM `unit_weights`
                """.trimIndent()
            )

            db.execSQL("DROP TABLE IF EXISTS `unit_weights`")
            db.execSQL("DROP TABLE IF EXISTS `selections`")
            db.execSQL("DROP TABLE IF EXISTS `trades`")
            db.execSQL("DROP TABLE IF EXISTS `shipments`")
            db.execSQL("DROP TABLE IF EXISTS `parties`")
            db.execSQL("DROP TABLE IF EXISTS `id_mappings`")

            db.execSQL("ALTER TABLE `parties_new` RENAME TO `parties`")
            db.execSQL("ALTER TABLE `shipments_new` RENAME TO `shipments`")
            db.execSQL("ALTER TABLE `trades_new` RENAME TO `trades`")
            db.execSQL("ALTER TABLE `selections_new` RENAME TO `selections`")
            db.execSQL("ALTER TABLE `unit_weights_new` RENAME TO `unit_weights`")

            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_parties_remoteId` ON `parties` (`remoteId`)")
            
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_shipments_remoteId` ON `shipments` (`remoteId`)")
            
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_trades_remoteId` ON `trades` (`remoteId`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_trades_shipmentLocalId` ON `trades` (`shipmentLocalId`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_trades_partyLocalId` ON `trades` (`partyLocalId`)")
            
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_selections_remoteId` ON `selections` (`remoteId`)")
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_selections_tradeLocalId_selectionTypeId` ON `selections` (`tradeLocalId`, `selectionTypeId`)")
            
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_unit_weights_selectionLocalId` ON `unit_weights` (`selectionLocalId`)")
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_unit_weights_remoteId` ON `unit_weights` (`remoteId`)")
        }
    }

    val MIGRATION_7_8 = object : Migration(7, 8) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `apuntes` (
                    `localId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `remoteId` INTEGER,
                    `userId` INTEGER NOT NULL,
                    `recordDate` TEXT NOT NULL,
                    `observations` TEXT,
                    `syncState` TEXT NOT NULL,
                    `lastSyncAttemptAt` TEXT,
                    `lastSyncedAt` TEXT,
                    `serverUpdatedAt` TEXT,
                    `syncError` TEXT
                )
                """.trimIndent()
            )
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `apunte_details` (
                    `localId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `remoteId` INTEGER,
                    `apunteLocalId` INTEGER NOT NULL,
                    `selectionTypeId` INTEGER NOT NULL,
                    `jabaCount` INTEGER NOT NULL,
                    `isEnabled` INTEGER NOT NULL,
                    FOREIGN KEY(`apunteLocalId`) REFERENCES `apuntes`(`localId`) ON UPDATE NO ACTION ON DELETE CASCADE
                )
                """.trimIndent()
            )
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_apuntes_remoteId` ON `apuntes` (`remoteId`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_apunte_details_apunteLocalId` ON `apunte_details` (`apunteLocalId`)")
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_apunte_details_remoteId` ON `apunte_details` (`remoteId`)")
        }
    }

    val MIGRATION_8_9 = object : Migration(8, 9) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE `apuntes` ADD COLUMN `authorName` TEXT")
        }
    }

    val MIGRATION_9_10 = object : Migration(9, 10) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `shipment_expenses` (
                    `localId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `remoteId` INTEGER,
                    `shipmentLocalId` INTEGER NOT NULL,
                    `category` TEXT NOT NULL,
                    `subcategory` TEXT,
                    `amount` REAL NOT NULL,
                    `quantity` REAL,
                    `unitPrice` REAL,
                    `description` TEXT,
                    `expenseDate` TEXT NOT NULL,
                    `paidByPartyLocalId` INTEGER,
                    `syncState` TEXT NOT NULL,
                    `lastSyncAttemptAt` TEXT,
                    `lastSyncedAt` TEXT,
                    `serverUpdatedAt` TEXT,
                    `syncError` TEXT,
                    FOREIGN KEY(`shipmentLocalId`) REFERENCES `shipments`(`localId`) ON UPDATE NO ACTION ON DELETE CASCADE
                )
                """.trimIndent()
            )
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_shipment_expenses_remoteId` ON `shipment_expenses` (`remoteId`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_shipment_expenses_shipmentLocalId` ON `shipment_expenses` (`shipmentLocalId`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_shipment_expenses_category` ON `shipment_expenses` (`category`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_shipment_expenses_expenseDate` ON `shipment_expenses` (`expenseDate`)")
        }
    }

    val MIGRATION_10_11 = object : Migration(10, 11) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `workers` (
                    `localId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `remoteId` INTEGER,
                    `fullName` TEXT NOT NULL,
                    `dni` TEXT,
                    `phone` TEXT,
                    `isActive` INTEGER NOT NULL,
                    `notes` TEXT,
                    `syncState` TEXT NOT NULL,
                    `lastSyncAttemptAt` TEXT,
                    `lastSyncedAt` TEXT,
                    `serverUpdatedAt` TEXT,
                    `syncError` TEXT
                )
                """.trimIndent()
            )
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `shipment_labor` (
                    `localId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `remoteId` INTEGER,
                    `shipmentLocalId` INTEGER NOT NULL,
                    `workerLocalId` INTEGER NOT NULL,
                    `workDate` TEXT NOT NULL,
                    `amount` REAL NOT NULL,
                    `notes` TEXT,
                    `syncState` TEXT NOT NULL,
                    `lastSyncAttemptAt` TEXT,
                    `lastSyncedAt` TEXT,
                    `serverUpdatedAt` TEXT,
                    `syncError` TEXT,
                    FOREIGN KEY(`shipmentLocalId`) REFERENCES `shipments`(`localId`) ON UPDATE NO ACTION ON DELETE CASCADE,
                    FOREIGN KEY(`workerLocalId`) REFERENCES `workers`(`localId`) ON UPDATE NO ACTION ON DELETE RESTRICT
                )
                """.trimIndent()
            )
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_workers_remoteId` ON `workers` (`remoteId`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_workers_fullName` ON `workers` (`fullName`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_workers_isActive` ON `workers` (`isActive`)")
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_shipment_labor_remoteId` ON `shipment_labor` (`remoteId`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_shipment_labor_shipmentLocalId` ON `shipment_labor` (`shipmentLocalId`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_shipment_labor_workerLocalId` ON `shipment_labor` (`workerLocalId`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_shipment_labor_workDate` ON `shipment_labor` (`workDate`)")
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_shipment_labor_shipmentLocalId_workerLocalId_workDate` ON `shipment_labor` (`shipmentLocalId`, `workerLocalId`, `workDate`)")
        }
    }

    @Provides
    @Singleton
    fun provideAvmDatabase(@ApplicationContext context: Context): AvmDatabase {
        return Room.databaseBuilder(
            context,
            AvmDatabase::class.java,
            "avm_database"
        ).addMigrations(MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10, MIGRATION_10_11)
         .fallbackToDestructiveMigration(dropAllTables = true)
         .build()
    }

    @Provides
    @Singleton
    fun provideSelectionDao(database: AvmDatabase): SelectionDao {
        return database.selectionDao
    }

    @Provides
    @Singleton
    fun providePartyDao(database: AvmDatabase): PartyDao {
        return database.partyDao
    }

    @Provides
    @Singleton
    fun provideWorkerDao(database: AvmDatabase): WorkerDao {
        return database.workerDao
    }

    @Provides
    @Singleton
    fun provideShipmentDao(database: AvmDatabase): ShipmentDao {
        return database.shipmentDao
    }

    @Provides
    @Singleton
    fun provideShipmentExpenseDao(database: AvmDatabase): ShipmentExpenseDao {
        return database.shipmentExpenseDao
    }

    @Provides
    @Singleton
    fun provideShipmentLaborDao(database: AvmDatabase): ShipmentLaborDao {
        return database.shipmentLaborDao
    }

    @Provides
    @Singleton
    fun provideTradeDao(database: AvmDatabase): TradeDao {
        return database.tradeDao
    }

    @Provides
    @Singleton
    fun provideApuntesDao(database: AvmDatabase): ApuntesDao {
        return database.apuntesDao
    }
}
