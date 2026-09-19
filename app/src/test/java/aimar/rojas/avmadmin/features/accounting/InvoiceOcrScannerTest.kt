package aimar.rojas.avmadmin.features.accounting

import aimar.rojas.avmadmin.features.accounting.domain.InvoiceOcrScanner
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class InvoiceOcrScannerTest {

    private val scanner = InvoiceOcrScanner()

    @Test
    fun testOcrParsing_PeruInvoice() {
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

        val lines = sampleText.lines()
        val parsed = scanner.parseRawText(sampleText, lines)

        assertEquals("20601234567", parsed.supplierRuc)
        assertEquals("F001", parsed.series)
        assertEquals("00045231", parsed.number)
        assertEquals("FACTURA", parsed.documentType)
        assertEquals("2026-09-18", parsed.issueDate)
        assertEquals("118.00", parsed.totalAmount)
        assertEquals("100.00", parsed.subtotal)
        assertEquals("18.00", parsed.taxAmount)
        assertTrue(parsed.supplierName.contains("ESTACION DE SERVICIOS EL SOL"))
    }
}
