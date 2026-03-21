package aimar.rojas.avmadmin.core.di

import aimar.rojas.avmadmin.core.data.local.AvmDatabase
import aimar.rojas.avmadmin.features.selections.data.local.SelectionDao
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
}
