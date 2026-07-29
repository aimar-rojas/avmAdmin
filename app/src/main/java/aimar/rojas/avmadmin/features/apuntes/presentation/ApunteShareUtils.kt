package aimar.rojas.avmadmin.features.apuntes.presentation

import aimar.rojas.avmadmin.features.apuntes.domain.model.Apunte
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.net.Uri
import android.widget.Toast
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.max

fun shareApunteCardToWhatsApp(context: Context, record: Apunte) {
    val imageUri = runCatching { createApunteImageUri(context, record) }
        .onFailure {
            Toast.makeText(context, "No se pudo preparar la imagen.", Toast.LENGTH_SHORT).show()
        }
        .getOrNull() ?: return

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, imageUri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        setPackage("com.whatsapp")
    }

    try {
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        val chooserIntent = Intent.createChooser(
            Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, imageUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            },
            "Compartir apunte"
        )
        context.startActivity(chooserIntent)
    }
}

private fun createApunteImageUri(context: Context, record: Apunte): Uri {
    val bitmap = drawApunteBitmap(record)
    val shareDir = File(context.cacheDir, "shared_images").apply { mkdirs() }
    val imageFile = File(shareDir, "apunte_${record.id}.png")
    FileOutputStream(imageFile).use { output ->
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
    }
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        imageFile
    )
}

private fun drawApunteBitmap(record: Apunte): Bitmap {
    val enabledDetails = record.details.filter { it.isEnabled && it.jabaCount > 0 }
    val detailsByType = enabledDetails.associateBy { it.selectionTypeId }
    val totalJabas = enabledDetails.sumOf { it.jabaCount }
    val observationLines = record.observations
        ?.takeIf { it.isNotBlank() }
        ?.wrapAt(maxChars = 46)
        .orEmpty()
    val visibleRows = ApunteSelectionDefaults.orderedTypes.count { detailsByType[it.id] != null }
    val width = 1080
    val height = max(
        620,
        260 + observationLines.size * 42 + visibleRows * 58 + 118
    )

    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.rgb(245, 247, 251)
    }
    canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), backgroundPaint)

    val cardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.WHITE
    }
    val cardRect = RectF(48f, 48f, width - 48f, height - 48f)
    canvas.drawRoundRect(cardRect, 28f, 28f, cardPaint)

    val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.rgb(30, 33, 40)
        textSize = 48f
        typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
    }
    val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.rgb(78, 84, 96)
        textSize = 34f
    }
    val quantityPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.rgb(30, 33, 40)
        textSize = 42f
        typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
    }
    val strongPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.rgb(30, 33, 40)
        textSize = 36f
        typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
    }

    var y = 126f
    canvas.drawText("Apunte rápido", 86f, y, titlePaint)
    y += 52f
    canvas.drawText(formatShareDate(record.recordDate), 86f, y, bodyPaint)

    if (observationLines.isNotEmpty()) {
        y += 58f
        canvas.drawText("Observaciones", 86f, y, strongPaint)
        observationLines.forEach { line ->
            y += 42f
            canvas.drawText(line, 86f, y, bodyPaint)
        }
    }

    y += 58f
    val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.rgb(224, 229, 238)
        strokeWidth = 3f
    }
    canvas.drawLine(86f, y, width - 86f, y, linePaint)
    y += 58f

    ApunteSelectionDefaults.orderedTypes.forEach { type ->
        val detail = detailsByType[type.id] ?: return@forEach
        val swatchPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = ApunteSelectionDefaults.colorFor(type.id).toArgb()
        }
        canvas.drawCircle(104f, y - 10f, 18f, swatchPaint)
        val amount = detail.jabaCount.toString()
        canvas.drawText(amount, 142f, y + 4f, quantityPaint)
        val nameX = max(232f, 142f + quantityPaint.measureText(amount) + 34f)
        canvas.drawText(type.name, nameX, y, bodyPaint)
        y += 58f
    }

    y += 20f
    val totalRect = RectF(86f, y, width - 86f, y + 82f)
    val totalPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.rgb(217, 229, 255)
    }
    canvas.drawRoundRect(totalRect, 18f, 18f, totalPaint)
    val totalLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.rgb(30, 68, 130)
        textSize = 34f
        typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
    }
    canvas.drawText("TOTAL", 118f, y + 53f, totalLabelPaint)
    val totalText = "$totalJabas jabas"
    canvas.drawText(totalText, width - 118f - totalLabelPaint.measureText(totalText), y + 53f, totalLabelPaint)

    return bitmap
}

private fun String.wrapAt(maxChars: Int): List<String> {
    val words = trim().split(Regex("\\s+"))
    val lines = mutableListOf<String>()
    var currentLine = ""
    words.forEach { word ->
        val candidate = if (currentLine.isBlank()) word else "$currentLine $word"
        if (candidate.length > maxChars && currentLine.isNotBlank()) {
            lines.add(currentLine)
            currentLine = word
        } else {
            currentLine = candidate
        }
    }
    if (currentLine.isNotBlank()) lines.add(currentLine)
    return lines.take(4)
}

private fun formatShareDate(dateStr: String): String {
    return try {
        val date = LocalDate.parse(dateStr.take(10))
        val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
        date.format(formatter)
    } catch (e: Exception) {
        dateStr
    }
}
