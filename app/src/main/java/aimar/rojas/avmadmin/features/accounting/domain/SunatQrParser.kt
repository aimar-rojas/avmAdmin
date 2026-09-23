package aimar.rojas.avmadmin.features.accounting.domain

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import aimar.rojas.avmadmin.features.accounting.domain.model.InvoiceOcrData
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.suspendCancellableCoroutine
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.regex.Pattern
import kotlin.coroutines.resume

object SunatQrParser {

    private val scanner by lazy {
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE, Barcode.FORMAT_PDF417, Barcode.FORMAT_DATA_MATRIX)
            .build()
        BarcodeScanning.getClient(options)
    }

    /**
     * Procesa una imagen o Bitmap en busca de códigos QR SUNAT.
     * Retorna datos estructurados con 100% de exactitud si se detecta un QR SUNAT válido.
     */
    suspend fun scanAndParseQr(context: Context, imageUri: Uri): InvoiceOcrData? =
        suspendCancellableCoroutine { continuation ->
            try {
                val inputImage = InputImage.fromFilePath(context, imageUri)
                scanner.process(inputImage)
                    .addOnSuccessListener { barcodes ->
                        val parsed = findAndParseSunatBarcode(barcodes)
                        continuation.resume(parsed)
                    }
                    .addOnFailureListener {
                        continuation.resume(null)
                    }
            } catch (e: Exception) {
                continuation.resume(null)
            }
        }

    suspend fun scanAndParseQrFromBitmap(bitmap: Bitmap): InvoiceOcrData? =
        suspendCancellableCoroutine { continuation ->
            try {
                val inputImage = InputImage.fromBitmap(bitmap, 0)
                scanner.process(inputImage)
                    .addOnSuccessListener { barcodes ->
                        val parsed = findAndParseSunatBarcode(barcodes)
                        continuation.resume(parsed)
                    }
                    .addOnFailureListener {
                        continuation.resume(null)
                    }
            } catch (e: Exception) {
                continuation.resume(null)
            }
        }

    private fun findAndParseSunatBarcode(barcodes: List<Barcode>): InvoiceOcrData? {
        for (barcode in barcodes) {
            val raw = barcode.rawValue ?: barcode.displayValue ?: continue
            val parsed = parseSunatQrString(raw)
            if (parsed != null) {
                return parsed
            }
        }
        return null
    }

    /**
     * Decodifica la cadena de QR SUNAT (Formato estándar con pipes, URLs de consulta o URL-encoded).
     * RUC_EMISOR|TIPO_DOC|SERIE|NUMERO|IGV|TOTAL|FECHA|TIPO_DOC_CLIENTE|NUM_DOC_CLIENTE|HASH
     */
    fun parseSunatQrString(rawString: String): InvoiceOcrData? {
        if (rawString.isBlank()) return null
        var clean = rawString.trim()

        // 1. Decodificar si viene con encoding URL (%7C -> |, %20 -> espacio, etc.)
        try {
            if (clean.contains("%")) {
                clean = java.net.URLDecoder.decode(clean, "UTF-8").trim()
            }
        } catch (_: Exception) {}

        // 2. Si es una URL que contiene pipes en los parámetros (ej: https://.../?d=2060...|01|F001|...)
        if (clean.contains("|")) {
            val pipeIndex = clean.indexOf("|")
            val possibleStart = clean.substring(0, pipeIndex)
            if (possibleStart.contains("=")) {
                clean = clean.substring(clean.lastIndexOf("=") + 1)
            }
            return parsePipeFormat(clean, rawString)
        }

        // 3. Si es una URL con parámetros de consulta estándar (ej: https://.../?ruc=20...&serie=F001&numero=123...)
        if (clean.startsWith("http", ignoreCase = true) && clean.contains("?")) {
            val urlParsed = parseUrlQueryParams(clean, rawString)
            if (urlParsed != null) return urlParsed
        }

        // 4. Si es un formato delimitado por guiones en URL (ej: /20608300393-01-F001-000123-145.50-2026-09-18)
        if (clean.contains("-")) {
            val dashParsed = parseDashFormat(clean, rawString)
            if (dashParsed != null) return dashParsed
        }

        return null
    }

    private fun parsePipeFormat(clean: String, rawString: String): InvoiceOcrData? {
        val parts = clean.split("|").map { it.trim() }
        if (parts.size < 6) return null

        val rucCandidate = parts[0]
        if (!Pattern.matches("""^(10|20)\d{9}$""", rucCandidate)) {
            return null
        }

        val docTypeCode = parts.getOrNull(1) ?: "01"
        val docType = mapDocType(docTypeCode)
        val series = parts.getOrNull(2)?.uppercase() ?: ""
        val number = parts.getOrNull(3) ?: ""
        val taxStr = cleanAmount(parts.getOrNull(4) ?: "")
        val totalStr = cleanAmount(parts.getOrNull(5) ?: "")
        val rawDate = parts.getOrNull(6) ?: ""
        val formattedDate = normalizeDate(rawDate)

        var total = totalStr
        var tax = taxStr
        var subtotal = ""

        val totalDouble = total.toDoubleOrNull() ?: 0.0
        val taxDouble = tax.toDoubleOrNull() ?: 0.0

        if (totalDouble > 0.0) {
            if (tax.isNotBlank()) {
                val subDouble = (totalDouble - taxDouble).coerceAtLeast(0.0)
                subtotal = String.format(Locale.US, "%.2f", subDouble)
                tax = String.format(Locale.US, "%.2f", taxDouble)
            } else if (docType == "FACTURA") {
                val subDouble = totalDouble / 1.18
                subtotal = String.format(Locale.US, "%.2f", subDouble)
                tax = String.format(Locale.US, "%.2f", totalDouble - subDouble)
            } else {
                subtotal = String.format(Locale.US, "%.2f", totalDouble)
                tax = "0.00"
            }
        }

        return InvoiceOcrData(
            supplierRuc = rucCandidate,
            supplierName = "",
            documentType = docType,
            series = series,
            number = number,
            issueDate = formattedDate,
            subtotal = subtotal,
            taxAmount = tax,
            totalAmount = total,
            rawText = rawString,
            isAiExtracted = false
        )
    }

    private fun parseUrlQueryParams(urlStr: String, rawString: String): InvoiceOcrData? {
        return try {
            val uri = android.net.Uri.parse(urlStr)
            val ruc = uri.getQueryParameter("ruc") ?: uri.getQueryParameter("r") ?: uri.getQueryParameter("RUC") ?: ""
            if (!Pattern.matches("""^(10|20)\d{9}$""", ruc)) return null

            val docTypeParam = uri.getQueryParameter("tipo") ?: uri.getQueryParameter("t") ?: uri.getQueryParameter("tipoDoc") ?: "01"
            val docType = mapDocType(docTypeParam)
            val series = (uri.getQueryParameter("serie") ?: uri.getQueryParameter("s") ?: "").uppercase()
            val number = uri.getQueryParameter("numero") ?: uri.getQueryParameter("n") ?: uri.getQueryParameter("num") ?: ""
            val total = cleanAmount(uri.getQueryParameter("monto") ?: uri.getQueryParameter("total") ?: uri.getQueryParameter("m") ?: "")
            val rawDate = uri.getQueryParameter("fecha") ?: uri.getQueryParameter("f") ?: ""
            val formattedDate = normalizeDate(rawDate)

            var subtotal = ""
            var tax = "0.00"
            val totalDouble = total.toDoubleOrNull() ?: 0.0
            if (totalDouble > 0.0 && docType == "FACTURA") {
                val subDouble = totalDouble / 1.18
                subtotal = String.format(Locale.US, "%.2f", subDouble)
                tax = String.format(Locale.US, "%.2f", totalDouble - subDouble)
            } else if (totalDouble > 0.0) {
                subtotal = String.format(Locale.US, "%.2f", totalDouble)
            }

            InvoiceOcrData(
                supplierRuc = ruc,
                supplierName = "",
                documentType = docType,
                series = series,
                number = number,
                issueDate = formattedDate,
                subtotal = subtotal,
                taxAmount = tax,
                totalAmount = total,
                rawText = rawString,
                isAiExtracted = false
            )
        } catch (_: Exception) {
            null
        }
    }

    private fun parseDashFormat(text: String, rawString: String): InvoiceOcrData? {
        // Busca patrón: 20608300393-01-F001-000123...
        val pattern = Pattern.compile("""\b((?:10|20)\d{9})-(0[1378]|R1)-([FEBT][A-Z0-9]{3})-(\d{1,8})-([0-9.]+)-(\d{4}[-/]\d{1,2}[-/]\d{1,2}|\d{1,2}[-/]\d{1,2}[-/]\d{4})\b""")
        val matcher = pattern.matcher(text)
        if (matcher.find()) {
            val ruc = matcher.group(1) ?: ""
            val docType = mapDocType(matcher.group(2) ?: "01")
            val series = matcher.group(3)?.uppercase() ?: ""
            val number = matcher.group(4) ?: ""
            val total = cleanAmount(matcher.group(5) ?: "")
            val formattedDate = normalizeDate(matcher.group(6) ?: "")

            var subtotal = ""
            var tax = "0.00"
            val totalDouble = total.toDoubleOrNull() ?: 0.0
            if (totalDouble > 0.0 && docType == "FACTURA") {
                val subDouble = totalDouble / 1.18
                subtotal = String.format(Locale.US, "%.2f", subDouble)
                tax = String.format(Locale.US, "%.2f", totalDouble - subDouble)
            } else if (totalDouble > 0.0) {
                subtotal = String.format(Locale.US, "%.2f", totalDouble)
            }

            return InvoiceOcrData(
                supplierRuc = ruc,
                supplierName = "",
                documentType = docType,
                series = series,
                number = number,
                issueDate = formattedDate,
                subtotal = subtotal,
                taxAmount = tax,
                totalAmount = total,
                rawText = rawString,
                isAiExtracted = false
            )
        }
        return null
    }

    private fun mapDocType(code: String): String {
        return when (code.uppercase()) {
            "01", "FACTURA", "F" -> "FACTURA"
            "03", "BOLETA", "B" -> "BOLETA"
            "07", "NC" -> "NOTA_CREDITO"
            "08", "ND" -> "NOTA_DEBITO"
            "R1", "02", "RH", "RECIBO" -> "RECIBO_HONORARIOS"
            "TICK", "TICKET" -> "TICKET"
            else -> if (code.startsWith("F")) "FACTURA" else if (code.startsWith("B")) "BOLETA" else "FACTURA"
        }
    }

    private fun normalizeDate(dateStr: String): String {
        if (dateStr.isBlank()) {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            return sdf.format(java.util.Date())
        }

        // Formato YYYY-MM-DD
        val ymdPattern = Pattern.compile("""^(\d{4})[-/.](\d{1,2})[-/.](\d{1,2})""")
        val ymdMatch = ymdPattern.matcher(dateStr)
        if (ymdMatch.find()) {
            val year = ymdMatch.group(1)
            val month = ymdMatch.group(2)?.padStart(2, '0')
            val day = ymdMatch.group(3)?.padStart(2, '0')
            return "$day/$month/$year"
        }

        // Formato DD-MM-YYYY
        val dmyPattern = Pattern.compile("""^(\d{1,2})[-/.](\d{1,2})[-/.](\d{4})""")
        val dmyMatch = dmyPattern.matcher(dateStr)
        if (dmyMatch.find()) {
            val day = dmyMatch.group(1)?.padStart(2, '0')
            val month = dmyMatch.group(2)?.padStart(2, '0')
            val year = dmyMatch.group(3)
            return "$day/$month/$year"
        }

        return dateStr
    }

    private fun cleanAmount(amountStr: String): String {
        var clean = amountStr.replace(",", ".").trim()
        val parts = clean.split(".")
        if (parts.size > 2) {
            clean = parts.dropLast(1).joinToString("") + "." + parts.last()
        }
        return clean
    }
}
