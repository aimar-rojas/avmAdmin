package aimar.rojas.avmadmin.core.di

import aimar.rojas.avmadmin.core.data.local.AvmDatabase
import aimar.rojas.avmadmin.features.selections.data.local.SelectionDao
import aimar.rojas.avmadmin.features.parties.data.local.PartyDao
import aimar.rojas.avmadmin.features.shipments.data.local.ShipmentDao
import aimar.rojas.avmadmin.features.trades.data.local.TradeDao
import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAvmDatabase(@ApplicationContext context: Context): AvmDatabase {
        return Room.databaseBuilder(
            context,
            AvmDatabase::class.java,
            "avm_database"
        ).fallbackToDestructiveMigration()
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
}
