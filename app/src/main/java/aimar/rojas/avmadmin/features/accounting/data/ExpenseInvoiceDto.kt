package aimar.rojas.avmadmin.features.accounting.data

import com.google.gson.annotations.SerializedName

data class ExpenseInvoicesListResponseDto(
    @SerializedName("invoices") val invoices: List<ExpenseInvoiceDto>?,
    @SerializedName("total") val total: Int?,
    @SerializedName("page") val page: Int?,
    @SerializedName("limit") val limit: Int?
)

data class ExpenseInvoiceCreateResponseDto(
    @SerializedName("message") val message: String?,
    @SerializedName("invoice") val invoice: ExpenseInvoiceDto?
)

data class ExpenseInvoiceDto(
    @SerializedName("id") val id: Int,
    @SerializedName("boss_id") val bossId: Int,
    @SerializedName("created_by_id") val createdById: Int,
    @SerializedName("document_type") val documentType: String?,
    @SerializedName("series") val series: String?,
    @SerializedName("number") val number: String?,
    @SerializedName("supplier_ruc") val supplierRuc: String?,
    @SerializedName("supplier_name") val supplierName: String?,
    @SerializedName("issue_date") val issueDate: String?,
    @SerializedName("accounting_period") val accountingPeriod: String?,
    @SerializedName("currency") val currency: String?,
    @SerializedName("subtotal") val subtotal: Double?,
    @SerializedName("tax_amount") val taxAmount: Double?,
    @SerializedName("total_amount") val totalAmount: Double,
    @SerializedName("category") val category: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("storage_key") val storageKey: String?,
    @SerializedName("file_url") val fileUrl: String?,
    @SerializedName("status") val status: String?,
    @SerializedName("verified_at") val verifiedAt: String?
)

data class ExpenseInvoiceSummaryDto(
    @SerializedName("period") val period: String,
    @SerializedName("total_amount") val totalAmount: Double,
    @SerializedName("total_tax") val totalTax: Double,
    @SerializedName("total_subtotal") val totalSubtotal: Double,
    @SerializedName("total_count") val totalCount: Int,
    @SerializedName("by_category") val byCategory: List<CategorySummaryDto>?,
    @SerializedName("by_document_type") val byDocumentType: List<DocumentTypeSummaryDto>?
)

data class CategorySummaryDto(
    @SerializedName("category") val category: String,
    @SerializedName("total_amount") val totalAmount: Double,
    @SerializedName("count") val count: Int
)

data class DocumentTypeSummaryDto(
    @SerializedName("document_type") val documentType: String,
    @SerializedName("count") val count: Int
)
