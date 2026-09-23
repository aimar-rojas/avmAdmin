package aimar.rojas.avmadmin.features.accounting.presentation

import android.net.Uri
import aimar.rojas.avmadmin.features.accounting.domain.model.ExpenseInvoice
import aimar.rojas.avmadmin.features.accounting.domain.model.MonthlyInvoiceSummary

enum class InvoiceProcessingStage {
    IDLE,
    PREPARING_IMAGE,
    DECODING_SUNAT_QR,
    ANALYZING_WITH_AI,
    USING_LOCAL_OCR,
    COMPLETED
}

enum class InvoiceExtractionSource {
    SUNAT_QR,
    AI,
    LOCAL_OCR
}

data class ExpenseInvoicesUiState(
    val isLoading: Boolean = false,
    val invoices: List<ExpenseInvoice> = emptyList(),
    val summary: MonthlyInvoiceSummary? = null,
    val selectedPeriod: String = "",
    val selectedCategory: String = "",
    val error: String? = null
)

data class ScanInvoiceFormUiState(
    val scannedImageUri: Uri? = null,
    val originalFileUri: Uri? = null,
    val isPdf: Boolean = false,
    val isOcrProcessing: Boolean = false,
    val processingStage: InvoiceProcessingStage = InvoiceProcessingStage.IDLE,
    val extractionSource: InvoiceExtractionSource? = null,
    val isSubmitting: Boolean = false,
    val supplierRuc: String = "",
    val supplierName: String = "",
    val documentType: String = "FACTURA",
    val series: String = "",
    val number: String = "",
    val issueDate: String = "",
    val subtotal: String = "",
    val taxAmount: String = "",
    val totalAmount: String = "",
    val currency: String = "PEN",
    val category: String = "OTROS",
    val description: String = "",
    val isSuccess: Boolean = false,
    val showDuplicateInvoiceSheet: Boolean = false,
    val errorMessage: String? = null
)
