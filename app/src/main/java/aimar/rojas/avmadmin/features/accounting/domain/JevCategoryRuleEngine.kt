package aimar.rojas.avmadmin.features.accounting.domain

import java.util.regex.Pattern

object JevCategoryRuleEngine {

    private val PLATE_KEYWORD_PATTERN = Pattern.compile(
        """(?:PLACA|VEH[IÍ]CULO|CAMI[OÓ]N|TRACTOR|UNIDAD|CARRETA|AUTO)\s*[:#.-]?\s*([A-Z0-9]{2,3}[- ]?[A-Z0-9]{3})""",
        Pattern.CASE_INSENSITIVE
    )

    private val STANDALONE_PLATE_PATTERN = Pattern.compile(
        """\b([A-Z][A-Z0-9]{2}[- ][A-Z0-9]{3})\b""",
        Pattern.CASE_INSENSITIVE
    )

    /**
     * Categorías vehiculares/operativas donde una placa tiene sentido contable.
     */
    private val VEHICLE_RELATED_CATEGORIES = setOf("COMBUSTIBLE", "FLETE", "MANTENIMIENTO")

    /**
     * Extrae la placa del vehículo si está presente en el comprobante.
     * Solo busca placas si:
     * 1. Viene precedida explícitamente por la palabra "PLACA", "VEHICULO", etc.
     * 2. O la categoría pertenece al ámbito vehicular/transporte (Combustible, Flete, Mantenimiento).
     */
    fun extractVehiclePlate(text: String, category: String = ""): String? {
        if (text.isBlank()) return null

        // 1. Prioridad Máxima: Buscar siempre si viene con palabra clave explícita (ej: "PLACA: T5A-890")
        val kwMatcher = PLATE_KEYWORD_PATTERN.matcher(text)
        if (kwMatcher.find()) {
            val candidate = kwMatcher.group(1)?.replace(" ", "-")?.uppercase()
            if (isValidPlateFormat(candidate)) {
                return normalizePlate(candidate)
            }
        }

        // 2. Si NO hay palabra clave explícita, SOLO buscar formato libre si la categoría es de transporte/combustible/taller
        if (category.uppercase() in VEHICLE_RELATED_CATEGORIES) {
            val standaloneMatcher = STANDALONE_PLATE_PATTERN.matcher(text)
            while (standaloneMatcher.find()) {
                val candidate = standaloneMatcher.group(1)?.replace(" ", "-")?.uppercase()
                if (isValidPlateFormat(candidate)) {
                    // Descartar si coincide con series de comprobantes (ej: F001-123 no es placa)
                    if (!isDocumentSeriesPrefix(candidate!!)) {
                        return normalizePlate(candidate)
                    }
                }
            }
        }

        return null
    }

    private fun isDocumentSeriesPrefix(candidate: String): Boolean {
        val upper = candidate.uppercase()
        return upper.startsWith("F0") || upper.startsWith("B0") || upper.startsWith("E0") ||
               upper.startsWith("EB") || upper.startsWith("00") || upper.startsWith("T0")
    }

    private fun isValidPlateFormat(plate: String?): Boolean {
        if (plate == null) return false
        val clean = plate.replace("-", "").trim()
        return clean.length in 5..6 && clean.matches(Regex("""^[A-Z0-9]+$"""))
    }

    private fun normalizePlate(plate: String?): String {
        if (plate == null) return ""
        val clean = plate.replace("-", "").trim().uppercase()
        return if (clean.length == 6) {
            "${clean.substring(0, 3)}-${clean.substring(3)}"
        } else {
            clean
        }
    }

    /**
     * Clasifica automáticamente la categoría de gasto basándose en:
     * - Razón social del emisor
     * - Texto completo del comprobante
     * - RUC del emisor
     */
    fun classifyCategory(supplierName: String, rawText: String): String {
        val combined = "$supplierName $rawText".uppercase()

        return when {
            // 1. Combustible y Lubricantes
            containsAny(combined, listOf(
                "GRIFO", "PRIMAX", "REPSOL", "PETROPERU", "PETROPERÚ", "SERVICENTRO",
                "GASOCENTRO", "COMBUSTIBLE", "DIESEL", "D-B5", "DB5", "GASOHOL", "GASOLINA",
                "GLP", "GNV", "ESTACION DE SERVICIOS", "ESTACION", "PETROLEO", "VALVULINA",
                "OCTANAJE", "SURTIDOR", "GALON", "GALONES"
            )) -> "COMBUSTIBLE"

            // 2. Fertilizantes, Abonos y Agroquímicos
            containsAny(combined, listOf(
                "FERTILIZANTE", "UREA", "ABONO", "NITRATO", "AGROQUIMICA", "AGROQUÍMICA",
                "AGRO", "PESTICIDA", "FUNGICIDA", "INSECTICIDA", "FOLIAR", "SULFATO",
                "FOSFATO", "AGROVETERINARIA", "SEMILLAS", "HERBICIDA", "GUANO", "POTASIO",
                "CLORURO", "MAGNESIO", "ACIDO FOSFORICO"
            )) -> "FERTILIZANTES"

            // 3. Flete y Transporte
            containsAny(combined, listOf(
                "FLETE", "TRANSPORTE", "TRANSPORTES", "LOGISTICA", "LOGÍSTICA", "CARGA",
                "MUDANZA", "ENCOMIENDA", "PEAJE", "COVIPERU", "RUTAS DE LIMA", "AUTOPISTA",
                "NORVIAL", "AUTONORTE"
            )) -> "FLETE"

            // 4. Herramientas y Envases de Cosecha
            containsAny(combined, listOf(
                "HERRAMIENTA", "ENVASE", "JAVAS", "CAJAS", "PARIHUELAS", "TIJERA DE PODAR",
                "SACOS", "MALLA", "FERRETERIA", "FERRETERÍA", "SODIMAC", "PROMART", "MAESTRO",
                "TUBERIA", "MANGUERA", "CINTA DE RIEGO", "VALVULA"
            )) -> "HERRAMIENTAS"

            // 5. Mantenimiento y Taller Mecánico
            containsAny(combined, listOf(
                "MANTENIMIENTO", "MECANICA", "MECÁNICA", "TALLER", "REPUESTO", "LLANTA",
                "ACEITE MOTOR", "LUBRICANTE", "BATERIA", "BATERÍA", "VULCANIZADORA",
                "RECTIFICACIONES", "TORNO", "FILTRO", "EMBRAGUE", "FRENOS", "ALINEAMIENTO"
            )) -> "MANTENIMIENTO"

            // 6. Servicios Públicos y Comunicaciones
            containsAny(combined, listOf(
                "LUZ", "AGUA", "ENEL", "HIDRANDINA", "ELECTROPERU", "SEDAPAL", "SEDALIB",
                "INTERNET", "CLARO", "MOVISTAR", "ENTEL", "TELEFONIA", "RECIBO DE LUZ", "ENOSA",
                "SEAL", "ELECTRONOROESTE"
            )) -> "SERVICIOS"

            // 7. Alimentación y Viáticos de Cuadrilla/Choferes
            containsAny(combined, listOf(
                "RESTAURANTE", "ALMUERZO", "MENU", "MENÚ", "COMIDA", "HOSPEDAJE", "HOTEL",
                "HOSTAL", "ALIMENTACION", "VIATICO", "POLLERIA", "CEVICHERIA", "CHIFA"
            )) -> "VIATICOS"

            else -> "OTROS"
        }
    }

    /**
     * Construye una descripción inteligente contextualizada por categoría y datos detectados.
     */
    fun buildSmartDescription(rawText: String, supplierName: String, category: String): String {
        val plate = extractVehiclePlate(rawText, category)
        val itemsDetected = extractLineItemsSummary(rawText, category)

        val parts = mutableListOf<String>()
        if (plate != null) {
            parts.add("Placa: $plate")
        }
        if (itemsDetected.isNotBlank()) {
            parts.add(itemsDetected)
        }

        return parts.joinToString(" - ")
    }

    private fun extractLineItemsSummary(rawText: String, category: String): String {
        val lines = rawText.lines().map { it.trim() }.filter { it.length in 4..65 }
        
        val categoryKeywords = when (category) {
            "COMBUSTIBLE" -> listOf("DIESEL", "GASOHOL", "GASOLINA", "GLP", "GNV", "DB5", "GALONES", "GALON")
            "FERTILIZANTES" -> listOf("UREA", "FERTILIZANTE", "ABONO", "NITRATO", "SULFATO", "FOLIAR", "AGROQUIMICA", "INSECTICIDA")
            "HERRAMIENTAS" -> listOf("JAVA", "CAJA", "SACO", "TIJERA", "MALLA", "HERRAMIENTA", "VALVULA", "TUBERIA", "CINTA")
            "FLETE" -> listOf("FLETE", "TRANSPORTE", "CARGA", "PEAJE", "ENCOMIENDA", "VIAJE")
            "MANTENIMIENTO" -> listOf("ACEITE", "FILTRO", "LLANTA", "BATERIA", "REPUESTO", "CAMBIO DE ACEITE", "TALLER")
            "VIATICOS" -> listOf("ALMUERZO", "CONSUMO", "MENU", "MENÚ", "HOSPEDAJE", "CENA", "DESAYUNO")
            "SERVICIOS" -> listOf("CONSUMO DE ENERGIA", "ENERGIA", "AGUA POTABLE", "SERVICIO DE INTERNET", "TELEFONIA")
            else -> listOf("PRODUCTO", "SERVICIO", "ARTICULO", "COMPRA")
        }

        // Buscar línea que contenga la palabra clave del rubro
        for (line in lines) {
            val upper = line.uppercase()
            for (kw in categoryKeywords) {
                if (upper.contains(kw) && !upper.contains("RUC") && !upper.contains("FACTURA") && !upper.contains("BOLETA") && !upper.contains("TOTAL")) {
                    return line
                }
            }
        }

        return ""
    }

    private fun containsAny(text: String, keywords: List<String>): Boolean {
        for (kw in keywords) {
            if (text.contains(kw, ignoreCase = true)) {
                return true
            }
        }
        return false
    }
}
