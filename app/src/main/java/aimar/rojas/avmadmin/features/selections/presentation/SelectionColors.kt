package aimar.rojas.avmadmin.features.selections.presentation

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

object SelectionColors {
    val SinPita = Color.Black
    val Verde = Color(0xFF4CAF50)
    val Blanco = Color(0xFFE0E0E0)
    val Rosado = Color(0xFFE91E63)
    val Naranja = Color(0xFFFF9800)
    val Azul = Color(0xFF2196F3)
    val Morado = Color(0xFF9C27B0)
    val Amarillo = Color(0xFFFFEB3B)
}

fun selectionColorFor(selectionTypeId: Int, selectionTypeName: String? = null): Color {
    val name = selectionTypeName
        ?.lowercase()
        ?.replace("á", "a")
        ?.replace("é", "e")
        ?.replace("í", "i")
        ?.replace("ó", "o")
        ?.replace("ú", "u")
        .orEmpty()

    return when {
        "sin pita" in name -> SelectionColors.SinPita
        "verde" in name -> SelectionColors.Verde
        "blanco" in name -> SelectionColors.Blanco
        "rosado" in name || "rojo" in name -> SelectionColors.Rosado
        "naranja" in name -> SelectionColors.Naranja
        "azul" in name -> SelectionColors.Azul
        "morado" in name -> SelectionColors.Morado
        "amarillo" in name -> SelectionColors.Amarillo
        else -> selectionColorForId(selectionTypeId)
    }
}

fun selectionColorArgbFor(selectionTypeId: Int, selectionTypeName: String? = null): Int {
    return selectionColorFor(selectionTypeId, selectionTypeName).toArgb()
}

fun isLightSelectionColor(color: Color): Boolean {
    return color == SelectionColors.Blanco || color == SelectionColors.Amarillo
}

private fun selectionColorForId(selectionTypeId: Int): Color {
    return when (selectionTypeId) {
        1 -> SelectionColors.SinPita
        2 -> SelectionColors.Verde
        3 -> SelectionColors.Blanco
        4 -> SelectionColors.Rosado
        5 -> SelectionColors.Naranja
        6 -> SelectionColors.Azul
        7 -> SelectionColors.Morado
        8 -> SelectionColors.Amarillo
        else -> SelectionColors.SinPita
    }
}
