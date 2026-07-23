package aimar.rojas.avmadmin.features.apuntes.presentation

import aimar.rojas.avmadmin.features.selections.presentation.SelectionTypeInfo
import androidx.compose.ui.graphics.Color

object ApunteSelectionDefaults {
    val orderedTypes = listOf(
        SelectionTypeInfo(2, "Verde"),
        SelectionTypeInfo(3, "Blanco"),
        SelectionTypeInfo(7, "Morado"),
        SelectionTypeInfo(6, "Azul"),
        SelectionTypeInfo(5, "Naranja"),
        SelectionTypeInfo(4, "Rojo / rosado"),
        SelectionTypeInfo(8, "Amarillo"),
        SelectionTypeInfo(1, "Sin pita")
    )

    fun colorFor(id: Int): Color {
        return when (id) {
            1 -> Color.Black
            2 -> Color(0xFF2E7D32)
            3 -> Color(0xFFE7E9ED)
            4 -> Color(0xFFE04F6D)
            5 -> Color(0xFFF28C28)
            6 -> Color(0xFF1E73D8)
            7 -> Color(0xFF6A3FA0)
            8 -> Color(0xFFF6D84A)
            else -> Color.Black
        }
    }
}
