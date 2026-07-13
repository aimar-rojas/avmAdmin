package aimar.rojas.avmadmin.core.di

import aimar.rojas.avmadmin.features.apuntes.data.ApuntesApiService
import aimar.rojas.avmadmin.features.apuntes.data.ApuntesRepositoryImpl
import aimar.rojas.avmadmin.features.apuntes.domain.ApuntesRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApuntesModule {

    @Provides
    @Singleton
    fun provideApuntesApiService(retrofit: Retrofit): ApuntesApiService {
        return retrofit.create(ApuntesApiService::class.java)
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class ApuntesRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindApuntesRepository(
        apuntesRepositoryImpl: ApuntesRepositoryImpl
    ): ApuntesRepository
}
