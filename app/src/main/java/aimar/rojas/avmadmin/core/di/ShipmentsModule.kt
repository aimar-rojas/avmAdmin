package aimar.rojas.avmadmin.core.di

import aimar.rojas.avmadmin.features.shipments.data.ShipmentsApiService
import aimar.rojas.avmadmin.features.shipments.data.ShipmentsRepositoryImpl
import aimar.rojas.avmadmin.features.shipments.domain.ShipmentsRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ShipmentsModule {

    @Provides
    @Singleton
    fun provideShipmentsApiService(retrofit: Retrofit): ShipmentsApiService {
        return retrofit.create(ShipmentsApiService::class.java)
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class ShipmentsRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindShipmentsRepository(
        shipmentsRepositoryImpl: ShipmentsRepositoryImpl
    ): ShipmentsRepository
}
