package aimar.rojas.avmadmin.features.accounting.presentation

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import aimar.rojas.avmadmin.features.accounting.data.ImageUtils
import aimar.rojas.avmadmin.features.accounting.domain.DuplicateExpenseInvoiceException
import aimar.rojas.avmadmin.features.accounting.domain.ExpenseInvoicesRepository
import aimar.rojas.avmadmin.features.accounting.domain.InvoiceOcrScanner
import aimar.rojas.avmadmin.features.accounting.domain.JevCategoryRuleEngine
import aimar.rojas.avmadmin.features.accounting.domain.SunatQrParser
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class ExpenseInvoicesViewModel @Inject constructor(
    private val repository: ExpenseInvoicesRepository,
    private val ocrScanner: InvoiceOcrScanner,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExpenseInvoicesUiState())
    val uiState: StateFlow<ExpenseInvoicesUiState> = _uiState.asStateFlow()

    private val _formState = MutableStateFlow(ScanInvoiceFormUiState())
    val formState: StateFlow<ScanInvoiceFormUiState> = _formState.asStateFlow()
    private var ocrJob: Job? = null

    init {
        val currentPeriod = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())
        _uiState.update { it.copy(selectedPeriod = currentPeriod) }
        loadInvoices(period = currentPeriod)
    }

    fun loadInvoices(period: String? = _uiState.value.selectedPeriod, category: String? = _uiState.value.selectedCategory) {
        val finalPeriod = period ?: _uiState.value.selectedPeriod
        _uiState.update { it.copy(isLoading = true, error = null, selectedPeriod = finalPeriod) }

        viewModelScope.launch {
            val listResult = repository.getInvoices(
                period = finalPeriod.ifEmpty { null },
                category = category?.ifEmpty { null }
            )
            val summaryResult = repository.getMonthlySummary(
                period = finalPeriod.ifEmpty { null }
            )

            listResult.fold(
                onSuccess = { invoices ->
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            invoices = invoices,
                            summary = summaryResult.getOrNull()
                        )
                    }
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = err.message ?: "Error al cargar facturas"
                        )
                    }
                }
            )
        }
    }

    fun onPeriodChanged(newPeriod: String) {
        _uiState.update { it.copy(selectedPeriod = newPeriod) }
        loadInvoices(period = newPeriod)
    }

    fun onCategoryFilterChanged(category: String) {
        val newCat = if (_uiState.value.selectedCategory == category) "" else category
        _uiState.update { it.copy(selectedCategory = newCat) }
        loadInvoices(category = newCat)
    }

    fun onImageScanned(imageUri: Uri) {
        onDocumentSelected(imageUri)
    }

    fun onDocumentSelected(documentUri: Uri) {
        ocrJob?.cancel()
        val isPdf = ImageUtils.isPdfUri(context, documentUri)

        _formState.update {
            ScanInvoiceFormUiState(
                scannedImageUri = documentUri,
                originalFileUri = documentUri,
                isPdf = isPdf,
                isOcrProcessing = true,
                processingStage = InvoiceProcessingStage.PREPARING_IMAGE
            )
        }

        ocrJob = viewModelScope.launch {
            try {
                // 1. Preparar archivo de previsualización (WebP) y bitmap para análisis
                val previewUri: Uri
                val previewWebpFile: File
                var previewBitmap: Bitmap? = null

                if (isPdf) {
                    val renderedBitmap = withContext(Dispatchers.IO) {
                        ImageUtils.renderPdfFirstPageToBitmap(context, documentUri)
                    }
                    previewBitmap = renderedBitmap
                    previewWebpFile = withContext(Dispatchers.IO) {
                        ImageUtils.renderPdfFirstPageToWebp(context, documentUri)
                    }
                    previewUri = Uri.fromFile(previewWebpFile)
                } else {
                    previewWebpFile = withContext(Dispatchers.IO) {
                        ImageUtils.compressAndSaveToWebp(context, documentUri, enableSmartEnhancement = true)
                    }
                    previewUri = Uri.fromFile(previewWebpFile)
                }

                _formState.update {
                    it.copy(
                        scannedImageUri = previewUri,
                        processingStage = InvoiceProcessingStage.DECODING_SUNAT_QR
                    )
                }

                // 2. PASO 1 ($0 tokens, 100% exactitud): Escanear y Decodificar Código QR SUNAT
                val qrData = withContext(Dispatchers.IO) {
                    if (previewBitmap != null) {
                        SunatQrParser.scanAndParseQrFromBitmap(previewBitmap)
                    } else {
                        SunatQrParser.scanAndParseQr(context, previewUri)
                    }
                }

                if (qrData != null && qrData.supplierRuc.isNotBlank()) {
                    // Si encontramos el QR SUNAT, el RUC emisor, serie, número, fecha y montos son 100% exactos.
                    // Usamos OCR local complementario (<50ms) solo para extraer la Razón Social, Placa y Descripción
                    val localOcrData = withContext(Dispatchers.IO) {
                        try {
                            if (previewBitmap != null) {
                                ocrScanner.processBitmap(previewBitmap)
                            } else {
                                ocrScanner.processImage(context, previewUri)
                            }
                        } catch (_: Exception) {
                            null
                        }
                    }

                    val rawText = localOcrData?.rawText ?: qrData.rawText
                    val supplierName = localOcrData?.supplierName?.takeIf { it.isNotBlank() } ?: ""
                    val category = JevCategoryRuleEngine.classifyCategory(supplierName, rawText)
                    val description = JevCategoryRuleEngine.buildSmartDescription(rawText, supplierName, category)

                    val enrichedData = qrData.copy(
                        supplierName = supplierName,
                        category = category,
                        description = description,
                        rawText = rawText
                    )

                    previewBitmap?.recycle()
                    applyExtractedData(enrichedData, InvoiceExtractionSource.SUNAT_QR)
                    return@launch
                }

                // 3. PASO 2 (Sin QR detectado): Análisis Inteligente con IA
                _formState.update {
                    it.copy(processingStage = InvoiceProcessingStage.ANALYZING_WITH_AI)
                }

                var aiSuccess = false
                val aiResult = repository.parseInvoiceWithAi(previewWebpFile)
                aiResult.fold(
                    onSuccess = { ocrData ->
                        aiSuccess = true
                        previewBitmap?.recycle()
                        applyExtractedData(ocrData, InvoiceExtractionSource.AI)
                    },
                    onFailure = {
                        aiSuccess = false
                    }
                )

                // 4. PASO 3 (Fallback local): ML Kit Text Recognition + Motor de Reglas JEV
                if (!aiSuccess) {
                    _formState.update {
                        it.copy(processingStage = InvoiceProcessingStage.USING_LOCAL_OCR)
                    }

                    val localOcrData = withContext(Dispatchers.IO) {
                        if (previewBitmap != null) {
                            ocrScanner.processBitmap(previewBitmap)
                        } else {
                            ocrScanner.processImage(context, previewUri)
                        }
                    }

                    previewBitmap?.recycle()
                    applyExtractedData(localOcrData, InvoiceExtractionSource.LOCAL_OCR)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _formState.update {
                    it.copy(
                        isOcrProcessing = false,
                        processingStage = InvoiceProcessingStage.COMPLETED,
                        supplierRuc = it.supplierRuc.trim(),
                        supplierName = it.supplierName.trim(),
                        series = it.series.trim(),
                        number = it.number.trim(),
                        issueDate = it.issueDate.ifBlank { currentDateInPeruvianFormat() },
                        subtotal = it.subtotal.ifBlank { "0.00" },
                        taxAmount = it.taxAmount.ifBlank { "0.00" },
                        category = it.category.ifBlank { "OTROS" },
                        description = it.description.trim(),
                        errorMessage = "No se pudo leer el comprobante automáticamente. Puedes ingresar los datos manualmente."
                    )
                }
            }
        }
    }

    fun updateRuc(value: String) = _formState.update { it.copy(supplierRuc = value) }
    fun updateSupplierName(value: String) = _formState.update { it.copy(supplierName = value) }
    fun updateDocumentType(value: String) = _formState.update { it.copy(documentType = value) }
    fun updateSeries(value: String) = _formState.update { it.copy(series = value) }
    fun updateNumber(value: String) = _formState.update { it.copy(number = value) }
    fun updateIssueDate(value: String) = _formState.update { it.copy(issueDate = value) }
    fun updateCategory(value: String) = _formState.update { it.copy(category = value) }
    fun updateDescription(value: String) = _formState.update { it.copy(description = value) }
    fun updateCurrency(value: String) = _formState.update { it.copy(currency = value) }

    fun updateTotalAmount(value: String) {
        _formState.update { current ->
            val totalDouble = value.toDoubleOrNull() ?: 0.0
            var newSub = current.subtotal
            var newTax = current.taxAmount

            if (totalDouble > 0.0 && current.documentType == "FACTURA") {
                val calcSub = totalDouble / 1.18
                val calcTax = totalDouble - calcSub
                newSub = String.format(Locale.US, "%.2f", calcSub)
                newTax = String.format(Locale.US, "%.2f", calcTax)
            }

            current.copy(
                totalAmount = value,
                subtotal = newSub,
                taxAmount = newTax
            )
        }
    }

    fun updateSubtotal(value: String) = _formState.update { it.copy(subtotal = value) }
    fun updateTaxAmount(value: String) = _formState.update { it.copy(taxAmount = value) }

    fun resetForm() {
        _formState.value = ScanInvoiceFormUiState()
    }

    fun cancelOcrProcessing() {
        ocrJob?.cancel()
        ocrJob = null
        resetForm()
    }

    fun submitInvoice(onSuccess: () -> Unit) {
        val state = _formState.value
        if (state.isOcrProcessing) return

        val uri = state.originalFileUri ?: state.scannedImageUri

        if (uri == null) {
            _formState.update { it.copy(errorMessage = "Debes escanear o seleccionar un comprobante") }
            return
        }

        val totalAmount = state.totalAmount.toDoubleOrNull()
        if (totalAmount == null || totalAmount <= 0.0) {
            _formState.update { it.copy(errorMessage = "El monto total debe ser mayor a 0") }
            return
        }

        _formState.update {
            it.copy(
                isSubmitting = true,
                errorMessage = null,
                showDuplicateInvoiceSheet = false
            )
        }

        viewModelScope.launch {
            try {
                // 1. Preparar archivo para subir: PDF original o imagen WebP comprimida
                val fileToUpload = withContext(Dispatchers.IO) {
                    if (state.isPdf && state.originalFileUri != null) {
                        ImageUtils.copyPdfToCache(context, state.originalFileUri)
                    } else {
                        ImageUtils.compressAndSaveToWebp(context, uri)
                    }
                }

                val subtotal = state.subtotal.toDoubleOrNull()
                val taxAmount = state.taxAmount.toDoubleOrNull()
                val period = extractPeriodFromDate(state.issueDate)

                // 2. Enviar a backend
                val result = repository.createInvoice(
                    file = fileToUpload,
                    totalAmount = totalAmount,
                    subtotal = subtotal,
                    taxAmount = taxAmount,
                    documentType = state.documentType,
                    series = state.series.ifEmpty { null },
                    number = state.number.ifEmpty { null },
                    supplierRuc = state.supplierRuc.ifEmpty { null },
                    supplierName = state.supplierName.ifEmpty { null },
                    issueDate = state.issueDate.ifEmpty { null },
                    accountingPeriod = period,
                    currency = "PEN",
                    category = state.category,
                    description = state.description.ifEmpty { null }
                )

                // Limpiar archivo temporal
                fileToUpload.delete()

                result.fold(
                    onSuccess = {
                        _formState.update { it.copy(isSubmitting = false, isSuccess = true) }
                        loadInvoices()
                        onSuccess()
                    },
                    onFailure = { err ->
                        _formState.update {
                            it.copy(
                                isSubmitting = false,
                                showDuplicateInvoiceSheet = err is DuplicateExpenseInvoiceException,
                                errorMessage = if (err is DuplicateExpenseInvoiceException) {
                                    null
                                } else {
                                    "No se pudo guardar el comprobante. Inténtalo nuevamente."
                                }
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                _formState.update {
                    it.copy(
                        isSubmitting = false,
                        errorMessage = "No se pudo preparar el comprobante. Inténtalo nuevamente."
                    )
                }
            }
        }
    }

    fun dismissDuplicateInvoiceSheet() {
        _formState.update { it.copy(showDuplicateInvoiceSheet = false) }
    }

    fun deleteInvoice(id: Long, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = repository.deleteInvoice(id)
            result.fold(
                onSuccess = {
                    loadInvoices()
                    onDone()
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = err.message ?: "Error al eliminar comprobante"
                        )
                    }
                    onDone()
                }
            )
        }
    }

    private fun extractPeriodFromDate(dateStr: String): String? {
        if (dateStr.isBlank()) return null
        val dmyParts = dateStr.split("/")
        if (dmyParts.size == 3) {
            val month = dmyParts[1].padStart(2, '0')
            val year = dmyParts[2]
            if (year.length == 4 && month.length == 2) {
                return "$year-$month"
            }
        }
        val ymdParts = dateStr.split("-")
        if (ymdParts.size == 3 && ymdParts[0].length == 4) {
            return "${ymdParts[0]}-${ymdParts[1].padStart(2, '0')}"
        }
        return null
    }

    private fun applyExtractedData(
        ocrData: aimar.rojas.avmadmin.features.accounting.domain.model.InvoiceOcrData,
        source: InvoiceExtractionSource
    ) {
        _formState.update { current ->
            current.copy(
                isOcrProcessing = false,
                processingStage = InvoiceProcessingStage.COMPLETED,
                extractionSource = source,
                supplierRuc = ocrData.supplierRuc.trim(),
                supplierName = ocrData.supplierName.trim(),
                documentType = ocrData.documentType.ifBlank { "FACTURA" },
                series = ocrData.series.trim(),
                number = ocrData.number.trim(),
                issueDate = ocrData.issueDate.ifBlank { currentDateInPeruvianFormat() },
                subtotal = ocrData.subtotal.ifBlank { "0.00" },
                taxAmount = ocrData.taxAmount.ifBlank { "0.00" },
                totalAmount = ocrData.totalAmount.trim(),
                category = ocrData.category.normalizedCategory(),
                description = ocrData.description.trim()
            )
        }
    }

    private fun String.orDash(): String = trim().ifBlank { "-" }

    private fun String.normalizedCategory(): String {
        val category = trim().uppercase()
        return category.takeIf { it in expenseInvoiceCategories } ?: "OTROS"
    }

    private fun currentDateInPeruvianFormat(): String =
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())

    private companion object {
        val expenseInvoiceCategories = setOf(
            "COMBUSTIBLE",
            "FLETE",
            "HERRAMIENTAS",
            "FERTILIZANTES",
            "MANTENIMIENTO",
            "SERVICIOS",
            "VIATICOS",
            "OTROS"
        )
    }
}
