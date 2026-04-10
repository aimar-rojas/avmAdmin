package aimar.rojas.avmadmin.core.di

import aimar.rojas.avmadmin.core.data.local.AvmDatabase
import aimar.rojas.avmadmin.features.selections.data.local.SelectionDao
import aimar.rojas.avmadmin.core.data.local.dao.IdMappingDao
import aimar.rojas.avmadmin.features.parties.data.local.PartyDao
import aimar.rojas.avmadmin.features.shipments.data.local.ShipmentDao
import aimar.rojas.avmadmin.features.trades.data.local.TradeDao
import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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

    @Provides
    @Singleton
    fun provideAvmDatabase(@ApplicationContext context: Context): AvmDatabase {
        return Room.databaseBuilder(
            context,
            AvmDatabase::class.java,
            "avm_database"
        ).addMigrations(MIGRATION_4_5, MIGRATION_5_6)
         .fallbackToDestructiveMigration()
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
    fun provideShipmentDao(database: AvmDatabase): ShipmentDao {
        return database.shipmentDao
    }

    @Provides
    @Singleton
    fun provideTradeDao(database: AvmDatabase): TradeDao {
        return database.tradeDao
    }

    @Provides
    @Singleton
    fun provideIdMappingDao(database: AvmDatabase): IdMappingDao {
        return database.idMappingDao
    }
}
