package aimar.rojas.avmadmin.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateUtils {
    
    /**
     * Formato usado para comunicarse con el API (formato de base de datos)
     * Formato: yyyy-MM-dd (ejemplo: 2024-01-15)
     */
    private val apiDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    
    /**
     * Formato usado para mostrar fechas en la UI (formato legible)
     * Formato: dd/MM/yyyy (ejemplo: 15/01/2024)
     */
    private val displayDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    
    /**
     * Parsea una fecha desde el formato del API (yyyy-MM-dd) a un objeto Date
     * @param dateString Fecha en formato yyyy-MM-dd
     * @return Date parseado o null si el formato es inválido
     */
    fun parseApiDate(dateString: String?): Date? {
        return dateString?.let { apiDateFormat.parse(it) }
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
     * Formatea un objeto Date al formato de visualización (dd/MM/yyyy)
     * @param date Fecha a formatear
     * @return String en formato dd/MM/yyyy
     */
    fun formatToDisplayDate(date: Date): String {
        return displayDateFormat.format(date)
    }
    
    /**
     * Convierte una fecha del formato API al formato de visualización
     * @param apiDateString Fecha en formato yyyy-MM-dd
     * @return String en formato dd/MM/yyyy o null si el formato es inválido
     */
    fun convertApiToDisplayDate(apiDateString: String?): String? {
        return parseApiDate(apiDateString)?.let { formatToDisplayDate(it) }
    }
}
