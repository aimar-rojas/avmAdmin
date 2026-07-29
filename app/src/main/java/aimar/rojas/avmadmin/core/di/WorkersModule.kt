package aimar.rojas.avmadmin.core.di

import aimar.rojas.avmadmin.features.workers.data.WorkersApiService
import aimar.rojas.avmadmin.features.workers.data.WorkersRepositoryImpl
import aimar.rojas.avmadmin.features.workers.domain.WorkersRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object WorkersApiModule {

    @Provides
    @Singleton
    fun provideWorkersApiService(retrofit: Retrofit): WorkersApiService {
        return retrofit.create(WorkersApiService::class.java)
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class WorkersRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindWorkersRepository(
        workersRepositoryImpl: WorkersRepositoryImpl
    ): WorkersRepository
}
