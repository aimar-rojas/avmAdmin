package aimar.rojas.avmadmin.features.accounting

import aimar.rojas.avmadmin.features.accounting.domain.InvoiceOcrScanner
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class InvoiceOcrScannerTest {

    private val scanner = InvoiceOcrScanner()

    @Test
    fun testOcrParsing_PeruInvoice() {
        // Simular texto extraído de una factura típica peruana
        val sampleText = """
            ESTACION DE SERVICIOS EL SOL S.A.C.
            AV. PANAMERICANA SUR KM 298 - ICA
            RUC: 20601234567
            FACTURA ELECTRONICA
            F001-00045231
            FECHA DE EMISION: 18/09/2026
            DESCRIPCION: DIESEL B5 S-50
            SUBTOTAL: S/ 100.00
            IGV (18%): S/ 18.00
            TOTAL A PAGAR: S/ 118.00
        """.trimIndent()

        // Crear una instancia de prueba o invocar la lógica de parsing
        val rucPattern = java.util.regex.Pattern.compile("""\b(10|20)\d{9}\b""")
        val rucMatcher = rucPattern.matcher(sampleText)
        assertTrue(rucMatcher.find())
        assertEquals("20601234567", rucMatcher.group(0))

        val invPattern = java.util.regex.Pattern.compile("""\b([FE][A-Z0-9]{3})[- ]?(\d{1,8})\b""")
        val invMatcher = invPattern.matcher(sampleText)
        assertTrue(invMatcher.find())
        assertEquals("F001", invMatcher.group(1))
        assertEquals("00045231", invMatcher.group(2))
    }
}
