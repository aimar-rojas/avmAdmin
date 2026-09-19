package aimar.rojas.avmadmin.features.accounting.domain

import aimar.rojas.avmadmin.features.accounting.domain.model.ExpenseInvoice
import aimar.rojas.avmadmin.features.accounting.domain.model.MonthlyInvoiceSummary
import java.io.File

interface ExpenseInvoicesRepository {
    suspend fun getInvoices(
        period: String? = null,
        category: String? = null,
        status: String? = null,
        page: Int? = 1,
        limit: Int? = 50
    ): Result<List<ExpenseInvoice>>

    suspend fun getMonthlySummary(period: String? = null): Result<MonthlyInvoiceSummary>

    suspend fun createInvoice(
        file: File,
        totalAmount: Double,
        subtotal: Double?,
        taxAmount: Double?,
        documentType: String,
        series: String?,
        number: String?,
        supplierRuc: String?,
        supplierName: String?,
        issueDate: String?,
        accountingPeriod: String?,
        currency: String?,
        category: String?,
        description: String?
    ): Result<ExpenseInvoice>
}
