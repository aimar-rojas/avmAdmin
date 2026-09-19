package aimar.rojas.avmadmin.features.accounting.data

import aimar.rojas.avmadmin.features.accounting.domain.ExpenseInvoicesRepository
import aimar.rojas.avmadmin.features.accounting.domain.model.CategorySummary
import aimar.rojas.avmadmin.features.accounting.domain.model.DocumentTypeSummary
import aimar.rojas.avmadmin.features.accounting.domain.model.ExpenseInvoice
import aimar.rojas.avmadmin.features.accounting.domain.model.MonthlyInvoiceSummary
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject

class ExpenseInvoicesRepositoryImpl @Inject constructor(
    private val apiService: ExpenseInvoicesApiService
) : ExpenseInvoicesRepository {

    override suspend fun getInvoices(
        period: String?,
        category: String?,
        status: String?,
        page: Int?,
        limit: Int?
    ): Result<List<ExpenseInvoice>> {
        return try {
            val response = apiService.getInvoices(period, category, status, page, limit)
            if (response.isSuccessful && response.body() != null) {
                val list = response.body()!!.invoices.orEmpty().map { it.toDomain() }
                Result.success(list)
            } else {
                Result.failure(Exception("Error al obtener facturas: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getMonthlySummary(period: String?): Result<MonthlyInvoiceSummary> {
        return try {
            val response = apiService.getSummary(period)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                val summary = MonthlyInvoiceSummary(
                    period = body.period,
                    totalAmount = body.totalAmount,
                    totalTax = body.totalTax,
                    totalSubtotal = body.totalSubtotal,
                    totalCount = body.totalCount,
                    byCategory = body.byCategory.orEmpty().map {
                        CategorySummary(it.category, it.totalAmount, it.count)
                    },
                    byDocumentType = body.byDocumentType.orEmpty().map {
                        DocumentTypeSummary(it.documentType, it.count)
                    }
                )
                Result.success(summary)
            } else {
                Result.failure(Exception("Error al obtener resumen: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createInvoice(
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
    ): Result<ExpenseInvoice> {
        return try {
            val textPlainMediaType = "text/plain".toMediaTypeOrNull()
            val imageMediaType = "image/webp".toMediaTypeOrNull()

            val fileRequestBody = file.asRequestBody(imageMediaType)
            val filePart = MultipartBody.Part.createFormData("file", file.name, fileRequestBody)

            val totalAmountBody = totalAmount.toString().toRequestBody(textPlainMediaType)
            val subtotalBody = subtotal?.toString()?.toRequestBody(textPlainMediaType)
            val taxAmountBody = taxAmount?.toString()?.toRequestBody(textPlainMediaType)
            val docTypeBody = documentType.toRequestBody(textPlainMediaType)
            val seriesBody = series?.toRequestBody(textPlainMediaType)
            val numberBody = number?.toRequestBody(textPlainMediaType)
            val rucBody = supplierRuc?.toRequestBody(textPlainMediaType)
            val supplierNameBody = supplierName?.toRequestBody(textPlainMediaType)
            val issueDateBody = issueDate?.toRequestBody(textPlainMediaType)
            val periodBody = accountingPeriod?.toRequestBody(textPlainMediaType)
            val currencyBody = (currency ?: "PEN").toRequestBody(textPlainMediaType)
            val categoryBody = (category ?: "OTROS").toRequestBody(textPlainMediaType)
            val descriptionBody = description?.toRequestBody(textPlainMediaType)

            val response = apiService.createInvoice(
                file = filePart,
                totalAmount = totalAmountBody,
                subtotal = subtotalBody,
                taxAmount = taxAmountBody,
                documentType = docTypeBody,
                series = seriesBody,
                number = numberBody,
                supplierRuc = rucBody,
                supplierName = supplierNameBody,
                issueDate = issueDateBody,
                accountingPeriod = periodBody,
                currency = currencyBody,
                category = categoryBody,
                description = descriptionBody
            )

            if (response.isSuccessful && response.body()?.invoice != null) {
                Result.success(response.body()!!.invoice!!.toDomain())
            } else {
                Result.failure(Exception("Error al registrar factura: ${response.code()} ${response.errorBody()?.string() ?: response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun ExpenseInvoiceDto.toDomain(): ExpenseInvoice {
        val rawUrl = fileUrl.orEmpty()
        val fullUrl = when {
            rawUrl.isEmpty() -> ""
            rawUrl.startsWith("http://") || rawUrl.startsWith("https://") -> rawUrl
            rawUrl.startsWith("/") -> "https://api.productosaimar.com$rawUrl"
            else -> "https://api.productosaimar.com/$rawUrl"
        }

        return ExpenseInvoice(
            id = id,
            bossId = bossId,
            documentType = documentType ?: "FACTURA",
            series = series.orEmpty(),
            number = number.orEmpty(),
            supplierRuc = supplierRuc.orEmpty(),
            supplierName = supplierName.orEmpty(),
            issueDate = issueDate.orEmpty(),
            accountingPeriod = accountingPeriod.orEmpty(),
            currency = currency ?: "PEN",
            subtotal = subtotal ?: 0.0,
            taxAmount = taxAmount ?: 0.0,
            totalAmount = totalAmount,
            category = category ?: "OTROS",
            description = description.orEmpty(),
            storageKey = storageKey.orEmpty(),
            fileUrl = fullUrl,
            status = status ?: "PENDING",
            verifiedAt = verifiedAt
        )
    }
}
