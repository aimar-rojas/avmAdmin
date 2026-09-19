package aimar.rojas.avmadmin.features.accounting.domain

import android.content.Context
import android.net.Uri
import aimar.rojas.avmadmin.features.accounting.domain.model.InvoiceOcrData
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.regex.Pattern

class InvoiceOcrScanner {

    private val recognizer by lazy {
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    }

    suspend fun processImage(context: Context, imageUri: Uri): InvoiceOcrData =
        suspendCancellableCoroutine { continuation ->
            try {
                val inputImage = InputImage.fromFilePath(context, imageUri)
                recognizer.process(inputImage)
                    .addOnSuccessListener { visionText ->
                        continuation.resume(parseInvoiceText(visionText))
                    }
                    .addOnFailureListener { exception ->
                        continuation.resumeWithException(exception)
                    }
            } catch (e: Exception) {
                continuation.resumeWithException(e)
            }
        }

    fun parseInvoiceText(visionText: Text): InvoiceOcrData {
        val fullText = visionText.text
        val lines = visionText.textBlocks.flatMap { it.lines.map { line -> line.text.trim() } }
        return parseRawText(fullText, lines)
    }

    fun parseRawText(fullText: String, lines: List<String>): InvoiceOcrData {
        var ruc = ""
        var supplierName = ""
        var docType = "FACTURA"
        var series = ""
        var number = ""
        var date = ""
        var subtotal = ""
        var tax = ""
        var total = ""

        // 1. RUC (11 dígitos, empieza con 10 o 20)
        val rucPattern = Pattern.compile("""\b(10|20)\d{9}\b""")
        val rucMatcher = rucPattern.matcher(fullText)
        if (rucMatcher.find()) {
            ruc = rucMatcher.group(0) ?: ""
        }

        // 2. Tipo de Documento, Serie y Número
        val invoicePattern = Pattern.compile("""\b([FE][A-Z0-9]{3})[- ]?(\d{1,8})\b""", Pattern.CASE_INSENSITIVE)
        val boletaPattern = Pattern.compile("""\b([B][A-Z0-9]{3})[- ]?(\d{1,8})\b""", Pattern.CASE_INSENSITIVE)
        val genericDocPattern = Pattern.compile("""\b([A-Z0-9]{3,4})[- ](\d{1,8})\b""")

        val invMatcher = invoicePattern.matcher(fullText)
        if (invMatcher.find()) {
            series = invMatcher.group(1)?.uppercase() ?: ""
            number = invMatcher.group(2) ?: ""
            docType = "FACTURA"
        } else {
            val bolMatcher = boletaPattern.matcher(fullText)
            if (bolMatcher.find()) {
                series = bolMatcher.group(1)?.uppercase() ?: ""
                number = bolMatcher.group(2) ?: ""
                docType = "BOLETA"
            } else {
                val genMatcher = genericDocPattern.matcher(fullText)
                if (genMatcher.find()) {
                    series = genMatcher.group(1)?.uppercase() ?: ""
                    number = genMatcher.group(2) ?: ""
                }
            }
        }

        if (fullText.contains("BOLETA DE VENTA", ignoreCase = true)) {
            docType = "BOLETA"
        } else if (fullText.contains("RECIBO POR HONORARIOS", ignoreCase = true)) {
            docType = "RECIBO_HONORARIOS"
        } else if (fullText.contains("TICKET", ignoreCase = true)) {
            docType = "TICKET"
        }

        // 3. Fecha de Emisión (DD/MM/YYYY o YYYY-MM-DD)
        val datePattern1 = Pattern.compile("""\b(\d{1,2})[/.-](\d{1,2})[/.-](\d{4})\b""")
        val datePattern2 = Pattern.compile("""\b(\d{4})[/.-](\d{1,2})[/.-](\d{1,2})\b""")

        val dMatcher1 = datePattern1.matcher(fullText)
        if (dMatcher1.find()) {
            val day = dMatcher1.group(1)?.padStart(2, '0')
            val month = dMatcher1.group(2)?.padStart(2, '0')
            val year = dMatcher1.group(3)
            date = "$year-$month-$day"
        } else {
            val dMatcher2 = datePattern2.matcher(fullText)
            if (dMatcher2.find()) {
                val year = dMatcher2.group(1)
                val month = dMatcher2.group(2)?.padStart(2, '0')
                val day = dMatcher2.group(3)?.padStart(2, '0')
                date = "$year-$month-$day"
            }
        }

        if (date.isEmpty()) {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            date = sdf.format(java.util.Date())
        }

        // 4. Montos (Total, Subtotal, IGV)
        val amountPattern = Pattern.compile("""(\d{1,6}(?:[.,]\d{2}))""")

        for (i in lines.indices) {
            val line = lines[i].uppercase()

            if (total.isEmpty() && !line.contains("SUBTOTAL") && (line.contains("TOTAL A PAGAR") || line.contains("IMPORTE TOTAL") || line.contains("TOTAL") || line.contains("VENTA TOTAL") || line.contains("TOTAL VENTA"))) {
                val match = amountPattern.matcher(line)
                if (match.find()) {
                    total = cleanAmount(match.group(1) ?: "")
                } else if (i + 1 < lines.size) {
                    val nextMatch = amountPattern.matcher(lines[i + 1])
                    if (nextMatch.find()) {
                        total = cleanAmount(nextMatch.group(1) ?: "")
                    }
                }
            }

            if (subtotal.isEmpty() && (line.contains("SUBTOTAL") || line.contains("OP. GRAVADA") || line.contains("OP GRAVADA") || line.contains("VALOR VENTA") || line.contains("GRAVADA"))) {
                val match = amountPattern.matcher(line)
                if (match.find()) {
                    subtotal = cleanAmount(match.group(1) ?: "")
                }
            }

            if (tax.isEmpty() && (line.contains("I.G.V") || line.contains("IGV") || line.contains("18%"))) {
                val match = amountPattern.matcher(line)
                if (match.find()) {
                    tax = cleanAmount(match.group(1) ?: "")
                }
            }
        }

        // Si tenemos total pero no subtotal o IGV, calculamos automáticamente los valores estándar (18% IGV en Perú)
        if (total.isNotEmpty()) {
            val totalDouble = total.toDoubleOrNull() ?: 0.0
            if (totalDouble > 0.0) {
                if (subtotal.isEmpty() && docType == "FACTURA") {
                    val calcSubtotal = totalDouble / 1.18
                    subtotal = String.format(Locale.US, "%.2f", calcSubtotal)
                }
                if (tax.isEmpty() && docType == "FACTURA") {
                    val subDouble = subtotal.toDoubleOrNull() ?: (totalDouble / 1.18)
                    val calcTax = totalDouble - subDouble
                    tax = String.format(Locale.US, "%.2f", calcTax)
                }
            }
        }

        // 5. Proveedor / Razón Social (Búsqueda inteligente en líneas de encabezado)
        for (line in lines.take(10)) {
            val upper = line.uppercase()
            if (upper.contains("S.A.C") || upper.contains("S.A.") || upper.contains("E.I.R.L") ||
                upper.contains("S.R.L") || upper.contains("GRIFO") || upper.contains("ESTACION") ||
                upper.contains("SERVICENTRO") || upper.contains("COMERCIAL") || upper.contains("DISTRIBUIDORA")
            ) {
                supplierName = line.replace(Regex("""(?i)RUC.*"""), "").trim()
                break
            }
        }

        if (supplierName.isEmpty() && lines.isNotEmpty()) {
            val candidate = lines.firstOrNull { it.length in 5..50 && !it.contains("RUC", ignoreCase = true) && !it.contains("FACTURA", ignoreCase = true) }
            if (candidate != null) {
                supplierName = candidate
            }
        }

        return InvoiceOcrData(
            supplierRuc = ruc,
            supplierName = supplierName,
            documentType = docType,
            series = series,
            number = number,
            issueDate = date,
            subtotal = subtotal,
            taxAmount = tax,
            totalAmount = total,
            rawText = fullText
        )
    }

    private fun cleanAmount(amountStr: String): String {
        var clean = amountStr.replace(",", ".")
        val parts = clean.split(".")
        if (parts.size > 2) {
            clean = parts.dropLast(1).joinToString("") + "." + parts.last()
        }
        return clean
    }
}
