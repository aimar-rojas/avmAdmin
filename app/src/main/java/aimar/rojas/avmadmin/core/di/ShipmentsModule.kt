package aimar.rojas.avmadmin.core.di

import aimar.rojas.avmadmin.features.shipments.data.ShipmentsRepositoryImpl
import aimar.rojas.avmadmin.features.shipments.data.ShipmentLaborApiService
import aimar.rojas.avmadmin.features.shipments.data.ShipmentLaborRepositoryImpl
import aimar.rojas.avmadmin.features.shipments.data.ShipmentExpensesRepositoryImpl
import aimar.rojas.avmadmin.features.shipments.data.ShipmentExpensesApiService
import aimar.rojas.avmadmin.features.shipments.domain.ShipmentLaborRepository
import aimar.rojas.avmadmin.features.shipments.domain.ShipmentExpensesRepository
import aimar.rojas.avmadmin.features.shipments.domain.ShipmentsRepository
import aimar.rojas.avmadmin.features.trades.data.TradesApiService
import aimar.rojas.avmadmin.features.trades.data.TradesRepositoryImpl
import aimar.rojas.avmadmin.features.trades.domain.TradesRepository
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
    fun provideShipmentsApiService(retrofit: Retrofit): aimar.rojas.avmadmin.features.shipments.data.ShipmentsApiService {
        return retrofit.create(aimar.rojas.avmadmin.features.shipments.data.ShipmentsApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideTradesApiService(retrofit: Retrofit): TradesApiService {
        return retrofit.create(TradesApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideShipmentExpensesApiService(retrofit: Retrofit): ShipmentExpensesApiService {
        return retrofit.create(ShipmentExpensesApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideShipmentLaborApiService(retrofit: Retrofit): ShipmentLaborApiService {
        return retrofit.create(ShipmentLaborApiService::class.java)
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

    @Binds
    @Singleton
    abstract fun bindTradesRepository(
        tradesRepositoryImpl: TradesRepositoryImpl
    ): TradesRepository

    @Binds
    @Singleton
    abstract fun bindShipmentExpensesRepository(
        shipmentExpensesRepositoryImpl: ShipmentExpensesRepositoryImpl
    ): ShipmentExpensesRepository

    @Binds
    @Singleton
    abstract fun bindShipmentLaborRepository(
        shipmentLaborRepositoryImpl: ShipmentLaborRepositoryImpl
    ): ShipmentLaborRepository
}
