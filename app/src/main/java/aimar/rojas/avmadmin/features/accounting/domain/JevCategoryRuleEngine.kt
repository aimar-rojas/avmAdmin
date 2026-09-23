package aimar.rojas.avmadmin.features.accounting.domain

import java.util.regex.Pattern

object JevCategoryRuleEngine {

    // Palabras clave estrictas para placas de vehículos (sin 'UNIDAD', 'AUTO', etc. que coinciden en boletas comunes)
    private val PLATE_KEYWORD_PATTERN = Pattern.compile(
        """\b(?:PLACA|NRO\.?\s*PLACA|N[°º]\s*PLACA|VEH[IÍ]CULO|TRACTOR|CAMI[OÓ]N)\s*[:#.-]?\s*([A-Z0-9]{2,3}[- ]?[A-Z0-9]{3,4})\b""",
        Pattern.CASE_INSENSITIVE
    )

    private val STANDALONE_PLATE_PATTERN = Pattern.compile(
        """\b([A-Z][A-Z0-9]{2}[- ][0-9]{3})\b""",
        Pattern.CASE_INSENSITIVE
    )

    /**
     * Categorías vehiculares/operativas donde una placa tiene sentido contable.
     * En cualquier otra categoría (OTROS, VIATICOS, FERTILIZANTES, HERRAMIENTAS, SERVICIOS), NO se extrae placa.
     */
    private val VEHICLE_RELATED_CATEGORIES = setOf("COMBUSTIBLE", "FLETE", "MANTENIMIENTO")

    // Palabras reservadas que NO son placas aunque tengan formato 6 caracteres
    private val BANNED_PLATE_PREFIXES = setOf(
        "NIU", "UND", "UNI", "CAN", "PAG", "TOT", "SUB", "IGV", "RUC", "DNI",
        "DOC", "SER", "NUM", "FEC", "HOR", "SOL", "PEN", "USD", "AUT", "IMP",
        "BOL", "FAC", "TIC", "MAS", "POS", "NET", "VTA", "COR", "BA4", "BA0",
        "FA0", "EA0", "EB0", "F00", "B00", "E00", "T00", "REF", "NRO"
    )

    /**
     * Extrae la placa del vehículo ÚNICAMENTE si la categoría es vehicular (Combustible, Flete, Mantenimiento).
     */
    fun extractVehiclePlate(text: String, category: String = ""): String? {
        if (text.isBlank()) return null
        
        val catUpper = category.uppercase().trim()
        // Si no es una categoría vehicular, NO extraemos placas bajo ninguna circunstancia
        if (catUpper !in VEHICLE_RELATED_CATEGORIES) {
            return null
        }

        // 1. Prioridad: Buscar si viene con palabra clave explícita (ej: "PLACA: T5A-890")
        val kwMatcher = PLATE_KEYWORD_PATTERN.matcher(text)
        if (kwMatcher.find()) {
            val candidate = kwMatcher.group(1)?.replace(" ", "-")?.uppercase()
            if (isValidPlateFormat(candidate)) {
                return normalizePlate(candidate)
            }
        }

        // 2. Si no hay palabra clave pero es combustible/flete/taller, buscar formato estricto (ej: "T5A-890")
        val standaloneMatcher = STANDALONE_PLATE_PATTERN.matcher(text)
        while (standaloneMatcher.find()) {
            val candidate = standaloneMatcher.group(1)?.replace(" ", "-")?.uppercase()
            if (isValidPlateFormat(candidate)) {
                return normalizePlate(candidate)
            }
        }

        return null
    }

    private fun isValidPlateFormat(plate: String?): Boolean {
        if (plate == null) return false
        val clean = plate.replace("-", "").replace(" ", "").trim().uppercase()
        if (clean.length !in 5..6) return false

        // Descartar palabras reservadas o prefijos de comprobantes
        val prefix3 = clean.take(3)
        if (prefix3 in BANNED_PLATE_PREFIXES) return false
        val prefix2 = clean.take(2)
        if (prefix2 in BANNED_PLATE_PREFIXES) return false

        // Formato peruano de vehículos:
        // 1. Regular M1/N1: 3 caracteres alfanuméricos (inicia con letra) + 3 dígitos (ej: T5A890, ABC123, V4B821)
        // 2. Motos: 2 caracteres + 4 dígitos (ej: 12345M, AB1234)
        val isStandardAuto = clean.matches(Regex("""^[A-Z][A-Z0-9]{2}[0-9]{3}$"""))
        val isClassicAuto = clean.matches(Regex("""^[A-Z]{3}[0-9]{3}$"""))
        val isMotorcycle = clean.matches(Regex("""^[A-Z0-9]{2}[0-9]{4}$"""))

        return isStandardAuto || isClassicAuto || isMotorcycle
    }

    private fun normalizePlate(plate: String?): String {
        if (plate == null) return ""
        val clean = plate.replace("-", "").replace(" ", "").trim().uppercase()
        return if (clean.length == 6) {
            "${clean.substring(0, 3)}-${clean.substring(3)}"
        } else {
            clean
        }
    }

    /**
     * Clasifica automáticamente la categoría de gasto basándose en palabras completas.
     */
    fun classifyCategory(supplierName: String, rawText: String): String {
        val combined = "$supplierName $rawText".uppercase()

        return when {
            // 1. Combustible y Lubricantes
            containsWord(combined, listOf(
                "GRIFO", "PRIMAX", "REPSOL", "PETROPERU", "PETROPERÚ", "SERVICENTRO",
                "GASOCENTRO", "COMBUSTIBLE", "DIESEL", "D-B5", "DB5", "GASOHOL", "GASOLINA",
                "GLP", "GNV", "PETROLEO", "OCTANAJE", "SURTIDOR", "GALON", "GALONES"
            )) -> "COMBUSTIBLE"

            // 2. Fertilizantes, Abonos y Agroquímicos
            containsWord(combined, listOf(
                "FERTILIZANTE", "FERTILIZANTES", "UREA", "ABONO", "ABONOS", "NITRATO", "AGROQUIMICA", "AGROQUÍMICA",
                "PESTICIDA", "FUNGICIDA", "INSECTICIDA", "FOLIAR", "SULFATO",
                "FOSFATO", "AGROVETERINARIA", "SEMILLAS", "HERBICIDA", "GUANO", "POTASIO",
                "CLORURO", "MAGNESIO"
            )) -> "FERTILIZANTES"

            // 3. Flete y Transporte
            containsWord(combined, listOf(
                "FLETE", "FLETES", "TRANSPORTE", "TRANSPORTES", "LOGISTICA", "LOGÍSTICA",
                "MUDANZA", "ENCOMIENDA", "PEAJE", "COVIPERU", "RUTAS DE LIMA", "AUTOPISTA",
                "NORVIAL", "AUTONORTE"
            )) -> "FLETE"

            // 4. Herramientas y Envases de Cosecha
            containsWord(combined, listOf(
                "HERRAMIENTA", "HERRAMIENTAS", "ENVASE", "ENVASES", "JAVAS", "CAJAS", "PARIHUELAS", "TIJERA DE PODAR",
                "SACOS", "MALLA", "FERRETERIA", "FERRETERÍA", "SODIMAC", "PROMART", "MAESTRO",
                "TUBERIA", "MANGUERA", "CINTA DE RIEGO", "VALVULA"
            )) -> "HERRAMIENTAS"

            // 5. Mantenimiento y Taller Mecánico
            containsWord(combined, listOf(
                "MANTENIMIENTO", "MECANICA", "MECÁNICA", "TALLER", "REPUESTO", "REPUESTOS", "LLANTA", "LLANTAS",
                "ACEITE MOTOR", "LUBRICANTE", "BATERIA", "BATERÍA", "VULCANIZADORA",
                "RECTIFICACIONES", "TORNO", "FILTRO", "EMBRAGUE", "FRENOS", "ALINEAMIENTO"
            )) -> "MANTENIMIENTO"

            // 6. Servicios Públicos y Comunicaciones
            containsWord(combined, listOf(
                "ENEL", "HIDRANDINA", "ELECTROPERU", "ELECTROCENTRO", "SEDAPAL", "SEDALIB",
                "INTERNET", "CLARO", "MOVISTAR", "ENTEL", "TELEFONIA", "RECIBO DE LUZ", "ENOSA",
                "SEAL", "ELECTRONOROESTE", "AGUA POTABLE"
            )) -> "SERVICIOS"

            // 7. Alimentación y Viáticos de Cuadrilla/Choferes
            containsWord(combined, listOf(
                "RESTAURANTE", "ALMUERZO", "MENU", "MENÚ", "COMIDA", "HOSPEDAJE", "HOTEL",
                "HOSTAL", "ALIMENTACION", "VIATICO", "POLLERIA", "CEVICHERIA", "CHIFA"
            )) -> "VIATICOS"

            else -> "OTROS"
        }
    }

    /**
     * Construye una descripción contextualizada por categoría y datos detectados.
     * Si la categoría es 'OTROS', no inventa descripciones artificiales ni placas.
     */
    fun buildSmartDescription(rawText: String, supplierName: String, category: String): String {
        val catUpper = category.uppercase().trim()
        if (catUpper !in VEHICLE_RELATED_CATEGORIES && catUpper == "OTROS") {
            return ""
        }

        val plate = if (catUpper in VEHICLE_RELATED_CATEGORIES) {
            extractVehiclePlate(rawText, catUpper)
        } else {
            null
        }
        val itemsDetected = extractLineItemsSummary(rawText, catUpper)

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
        if (category == "OTROS") {
            return ""
        }

        val lines = rawText.lines().map { it.trim() }.filter { it.length in 4..65 }

        val categoryKeywords = when (category) {
            "COMBUSTIBLE" -> listOf("DIESEL", "GASOHOL", "GASOLINA", "GLP", "GNV", "DB5", "GALONES", "GALON")
            "FERTILIZANTES" -> listOf("UREA", "FERTILIZANTE", "ABONO", "NITRATO", "SULFATO", "FOLIAR", "AGROQUIMICA", "INSECTICIDA")
            "HERRAMIENTAS" -> listOf("JAVA", "CAJA", "SACO", "TIJERA", "MALLA", "HERRAMIENTA", "VALVULA", "TUBERIA", "CINTA")
            "FLETE" -> listOf("FLETE", "TRANSPORTE", "CARGA", "PEAJE", "ENCOMIENDA", "VIAJE")
            "MANTENIMIENTO" -> listOf("ACEITE", "FILTRO", "LLANTA", "BATERIA", "REPUESTO", "CAMBIO DE ACEITE", "TALLER")
            "VIATICOS" -> listOf("ALMUERZO", "CONSUMO", "MENU", "MENÚ", "HOSPEDAJE", "CENA", "DESAYUNO")
            "SERVICIOS" -> listOf("CONSUMO DE ENERGIA", "ENERGIA", "AGUA POTABLE", "SERVICIO DE INTERNET", "TELEFONIA")
            else -> emptyList()
        }

        for (line in lines) {
            val upper = line.uppercase()
            for (kw in categoryKeywords) {
                if (upper.contains(kw) && !upper.contains("RUC") && !upper.contains("FACTURA") && !upper.contains("BOLETA") && !upper.contains("TOTAL") && !upper.contains("SUBTOTAL")) {
                    return line
                }
            }
        }

        return ""
    }

    private fun containsWord(text: String, keywords: List<String>): Boolean {
        for (kw in keywords) {
            val pattern = Pattern.compile("""\b${Pattern.quote(kw)}\b""", Pattern.CASE_INSENSITIVE)
            if (pattern.matcher(text).find()) {
                return true
            }
        }
        return false
    }
}
