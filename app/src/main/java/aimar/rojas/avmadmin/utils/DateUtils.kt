package aimar.rojas.avmadmin.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object DateUtils {
    
    /**
     * Formato usado para comunicarse con el API (formato de base de datos)
     * Formato: yyyy-MM-dd (ejemplo: 2024-01-15)
     */
    private val apiDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
        isLenient = false
    }
    
    /**
     * Formato usado para mostrar fechas en la UI (formato legible)
     * Formato: dd-MM-yyyy (ejemplo: 15-01-2024)
     */
    private val displayDateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).apply {
        isLenient = false
    }

    private const val syncTimestampPattern = "yyyy-MM-dd'T'HH:mm:ss'Z'"
    private const val displayDateRegex = """\d{2}-\d{2}-\d{4}"""
    
    /**
     * Parsea una fecha desde el formato del API (yyyy-MM-dd) a un objeto Date
     * @param dateString Fecha en formato yyyy-MM-dd
     * @return Date parseado o null si el formato es inválido
     */
    fun parseApiDate(dateString: String?): Date? {
        if (dateString.isNullOrBlank()) return null

        return try {
            apiDateFormat.parse(dateString)
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Formatea un objeto Date al formato del API (yyyy-MM-dd)
     * @param date Fecha a formatear
     * @return String en formato yyyy-MM-dd
     */
    fun formatToApiDate(date: Date): String {
        return apiDateFormat.format(date)
    }
    
    /**
     * Formatea un objeto Date al formato de visualización (dd-MM-yyyy)
     * @param date Fecha a formatear
     * @return String en formato dd-MM-yyyy
     */
    fun formatToDisplayDate(date: Date): String {
        return displayDateFormat.format(date)
    }
    
    /**
     * Convierte una fecha del formato API al formato de visualización
     * @param apiDateString Fecha en formato yyyy-MM-dd
     * @return String en formato dd-MM-yyyy o null si el formato es inválido
     */
    fun convertApiToDisplayDate(apiDateString: String?): String? {
        return parseApiDate(apiDateString)?.let { formatToDisplayDate(it) }
    }

    fun convertDisplayToApiDate(displayDateString: String?): String? {
        if (displayDateString.isNullOrBlank()) return null

        return try {
            displayDateFormat.parse(displayDateString)?.let { formatToApiDate(it) }
        } catch (e: Exception) {
            null
        }
    }

    fun isDisplayDate(value: String): Boolean {
        return value.matches(Regex(displayDateRegex)) && convertDisplayToApiDate(value) != null
    }

    /**
     * Material DatePicker entrega selectedDateMillis como medianoche UTC.
     * Convertimos esos componentes UTC a una fecha local al mediodía para evitar desfases por zona horaria.
     */
    fun dateFromPickerMillis(millis: Long): Date {
        val utcCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            timeInMillis = millis
        }
        return Calendar.getInstance().apply {
            clear()
            set(
                utcCalendar.get(Calendar.YEAR),
                utcCalendar.get(Calendar.MONTH),
                utcCalendar.get(Calendar.DAY_OF_MONTH),
                12,
                0,
                0
            )
        }.time
    }

    fun pickerMillisFromApiDate(apiDateString: String?): Long? {
        val date = parseApiDate(apiDateString) ?: return null
        val localCalendar = Calendar.getInstance().apply { time = date }
        return Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            clear()
            set(
                localCalendar.get(Calendar.YEAR),
                localCalendar.get(Calendar.MONTH),
                localCalendar.get(Calendar.DAY_OF_MONTH),
                0,
                0,
                0
            )
        }.timeInMillis
    }

    /**
     * Convierte timestamps UTC del sync a un formato legible para la UI.
     * Ejemplo: 2026-07-18T15:30:00Z -> 18-07-2026 10:30
     */
    fun formatSyncTimestampToDisplay(timestamp: String?): String? {
        if (timestamp.isNullOrBlank()) return null

        return try {
            val inputFormat = SimpleDateFormat(syncTimestampPattern, Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val outputFormat = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).apply {
                timeZone = TimeZone.getDefault()
            }
            inputFormat.parse(timestamp)?.let { outputFormat.format(it) }
        } catch (e: Exception) {
            null
        }
    }

    fun currentUtcSyncTimestamp(): String {
        return SimpleDateFormat(syncTimestampPattern, Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }.format(Date())
    }
}
