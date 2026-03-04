package aimar.rojas.avmadmin.core.di

import aimar.rojas.avmadmin.features.selections.data.SelectionsApiService
import aimar.rojas.avmadmin.features.selections.data.SelectionsRepositoryImpl
import aimar.rojas.avmadmin.features.selections.domain.SelectionsRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SelectionsModule {

    @Provides
    @Singleton
    fun provideSelectionsApiService(retrofit: Retrofit): SelectionsApiService {
        return retrofit.create(SelectionsApiService::class.java)
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class SelectionsRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindSelectionsRepository(
        impl: SelectionsRepositoryImpl
    ): SelectionsRepository
}
