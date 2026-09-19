package aimar.rojas.avmadmin.features.accounting.domain.model

data class InvoiceOcrData(
    val supplierRuc: String = "",
    val supplierName: String = "",
    val documentType: String = "FACTURA",
    val series: String = "",
    val number: String = "",
    val issueDate: String = "",
    val subtotal: String = "",
    val taxAmount: String = "",
    val totalAmount: String = "",
    val category: String = "",
    val description: String = "",
    val rawText: String = "",
    val isAiExtracted: Boolean = false
)
