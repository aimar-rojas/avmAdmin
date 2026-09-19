package aimar.rojas.avmadmin.core.di

import aimar.rojas.avmadmin.features.accounting.data.ExpenseInvoicesApiService
import aimar.rojas.avmadmin.features.accounting.data.ExpenseInvoicesRepositoryImpl
import aimar.rojas.avmadmin.features.accounting.domain.ExpenseInvoicesRepository
import aimar.rojas.avmadmin.features.accounting.domain.InvoiceOcrScanner
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AccountingModule {

    @Provides
    @Singleton
    fun provideExpenseInvoicesApiService(retrofit: Retrofit): ExpenseInvoicesApiService {
        return retrofit.create(ExpenseInvoicesApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideExpenseInvoicesRepository(
        apiService: ExpenseInvoicesApiService
    ): ExpenseInvoicesRepository {
        return ExpenseInvoicesRepositoryImpl(apiService)
    }

    @Provides
    @Singleton
    fun provideInvoiceOcrScanner(): InvoiceOcrScanner {
        return InvoiceOcrScanner()
    }
}
