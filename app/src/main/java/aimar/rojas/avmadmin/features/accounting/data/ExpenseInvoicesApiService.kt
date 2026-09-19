package aimar.rojas.avmadmin.features.accounting.data

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface ExpenseInvoicesApiService {

    @Multipart
    @POST("v1/expense-invoices")
    suspend fun createInvoice(
        @Part file: MultipartBody.Part,
        @Part("total_amount") totalAmount: RequestBody,
        @Part("subtotal") subtotal: RequestBody? = null,
        @Part("tax_amount") taxAmount: RequestBody? = null,
        @Part("document_type") documentType: RequestBody? = null,
        @Part("series") series: RequestBody? = null,
        @Part("number") number: RequestBody? = null,
        @Part("supplier_ruc") supplierRuc: RequestBody? = null,
        @Part("supplier_name") supplierName: RequestBody? = null,
        @Part("issue_date") issueDate: RequestBody? = null,
        @Part("accounting_period") accountingPeriod: RequestBody? = null,
        @Part("currency") currency: RequestBody? = null,
        @Part("category") category: RequestBody? = null,
        @Part("description") description: RequestBody? = null,
        @Part("boss_id") bossId: RequestBody? = null
    ): Response<ExpenseInvoiceCreateResponseDto>

    @GET("v1/expense-invoices")
    suspend fun getInvoices(
        @Query("period") period: String? = null,
        @Query("category") category: String? = null,
        @Query("status") status: String? = null,
        @Query("page") page: Int? = null,
        @Query("limit") limit: Int? = null
    ): Response<ExpenseInvoicesListResponseDto>

    @GET("v1/expense-invoices/summary")
    suspend fun getSummary(
        @Query("period") period: String? = null
    ): Response<ExpenseInvoiceSummaryDto>
}
