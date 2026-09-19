package aimar.rojas.avmadmin.features.accounting.domain.model

data class ExpenseInvoice(
    val id: Int,
    val bossId: Int,
    val documentType: String,
    val series: String,
    val number: String,
    val supplierRuc: String,
    val supplierName: String,
    val issueDate: String,
    val accountingPeriod: String,
    val currency: String,
    val subtotal: Double,
    val taxAmount: Double,
    val totalAmount: Double,
    val category: String,
    val description: String,
    val storageKey: String,
    val fileUrl: String,
    val status: String,
    val verifiedAt: String? = null
)

data class MonthlyInvoiceSummary(
    val period: String,
    val totalAmount: Double,
    val totalTax: Double,
    val totalSubtotal: Double,
    val totalCount: Int,
    val byCategory: List<CategorySummary>,
    val byDocumentType: List<DocumentTypeSummary>
)

data class CategorySummary(
    val category: String,
    val totalAmount: Double,
    val count: Int
)

data class DocumentTypeSummary(
    val documentType: String,
    val count: Int
)
