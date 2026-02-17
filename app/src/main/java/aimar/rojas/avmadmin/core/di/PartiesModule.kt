package aimar.rojas.avmadmin.core.di

import aimar.rojas.avmadmin.features.parties.data.PartiesApiService
import aimar.rojas.avmadmin.features.parties.data.PartiesRepositoryImpl
import aimar.rojas.avmadmin.features.parties.domain.PartiesRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PartiesModule {

    @Provides
    @Singleton
    fun providePartiesApiService(retrofit: Retrofit): PartiesApiService {
        return retrofit.create(PartiesApiService::class.java)
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class PartiesRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindPartiesRepository(
        partiesRepositoryImpl: PartiesRepositoryImpl
    ): PartiesRepository
}

