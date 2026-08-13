package aimar.rojas.avmadmin.features.selections.presentation

import aimar.rojas.avmadmin.domain.model.Trade
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.max

fun shareTradeSummaryToWhatsApp(
    context: Context,
    trade: Trade?,
    selections: List<SelectionSummaryItem>
) {
    if (trade == null || selections.isEmpty()) {
        Toast.makeText(context, "No hay resumen para compartir.", Toast.LENGTH_SHORT).show()
        return
    }

    val imageUri = runCatching { createTradeSummaryImageUri(context, trade, selections) }
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
            "Compartir resumen"
        )
        context.startActivity(chooserIntent)
    }
}

private fun createTradeSummaryImageUri(
    context: Context,
    trade: Trade,
    selections: List<SelectionSummaryItem>
): Uri {
    val bitmap = drawTradeSummaryBitmap(trade, selections)
    val shareDir = File(context.cacheDir, "shared_images").apply { mkdirs() }
    val imageFile = File(shareDir, "resumen_pesos_${trade.tradeId}.png")
    FileOutputStream(imageFile).use { output ->
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
    }
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        imageFile
    )
}

private fun drawTradeSummaryBitmap(
    trade: Trade,
    selections: List<SelectionSummaryItem>
): Bitmap {
    val width = 1080
    val detailRows = selections.sumOf { it.selectionDetail.unitWeights.size }
    val height = max(980, 470 + selections.size * 260 + detailRows * 34 + 210)

    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.rgb(245, 247, 251)
    }
    canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

    val cardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.WHITE
    }
    val cardRect = RectF(48f, 48f, width - 48f, height - 48f)
    canvas.drawRoundRect(cardRect, 28f, 28f, cardPaint)

    val titlePaint = textPaint(50f, android.graphics.Color.rgb(27, 31, 38), bold = true)
    val subtitlePaint = textPaint(30f, android.graphics.Color.rgb(90, 98, 112))
    val labelPaint = textPaint(28f, android.graphics.Color.rgb(94, 103, 118))
    val valuePaint = textPaint(32f, android.graphics.Color.rgb(31, 36, 44), bold = true)
    val totalPaint = textPaint(34f, android.graphics.Color.rgb(23, 84, 72), bold = true)
    val smallPaint = textPaint(25f, android.graphics.Color.rgb(105, 113, 128))
    val whiteTitlePaint = textPaint(34f, android.graphics.Color.WHITE, bold = true)
    val whiteSmallPaint = textPaint(25f, android.graphics.Color.rgb(228, 244, 239))

    var y = 126f
    canvas.drawText("Resumen de pesos", 86f, y, titlePaint)
    y += 46f
    val tradeType = if (trade.tradeType == "PURCHASE") "Compra" else "Venta"
    canvas.drawText(
        "$tradeType #${trade.remoteId ?: trade.tradeId} · ${formatShareDate(trade.startDatetime)}",
        86f,
        y,
        subtitlePaint
    )

    y += 66f
    val infoRect = RectF(86f, y, width - 86f, y + 126f)
    val infoPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.rgb(23, 116, 96)
    }
    canvas.drawRoundRect(infoRect, 22f, 22f, infoPaint)
    canvas.drawText("Variedad", 118f, y + 44f, whiteSmallPaint)
    canvas.drawText(trade.varietyAvocado, 118f, y + 88f, whiteTitlePaint)
    canvas.drawText("Descuento por jaba", 584f, y + 44f, whiteSmallPaint)
    canvas.drawText("${formatKg(trade.discountWeightPerTray)} kg", 584f, y + 88f, whiteTitlePaint)

    y += 168f
    val grossTotal = selections.sumOf { it.grossWeight }
    val crateTotal = selections.sumOf { it.crateCount }
    val discountTotal = selections.sumOf { it.grossWeight - it.netWeight }
    val netTotal = selections.sumOf { it.netWeight }
    val payTotal = selections.sumOf { it.totalToPay }

    val metricTop = y
    drawMetric(canvas, RectF(86f, metricTop, 314f, metricTop + 110f), "Bruto", "${formatKg(grossTotal)} kg", labelPaint, totalPaint)
    drawMetric(canvas, RectF(332f, metricTop, 506f, metricTop + 110f), "Jabas", crateTotal.toString(), labelPaint, totalPaint)
    drawMetric(canvas, RectF(524f, metricTop, 752f, metricTop + 110f), "Descuento", "${formatKg(discountTotal)} kg", labelPaint, totalPaint)
    drawMetric(canvas, RectF(770f, metricTop, width - 86f, metricTop + 110f), "Neto", "${formatKg(netTotal)} kg", labelPaint, totalPaint)

    y += 148f
    val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.rgb(224, 229, 238)
        strokeWidth = 3f
    }

    selections.forEach { item ->
        val rowTop = y
        val selectionPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = selectionColorArgbFor(
                item.selectionDetail.selectionTypeId,
                item.selectionDetail.selectionTypeName
            )
        }
        canvas.drawRoundRect(RectF(86f, rowTop, 100f, rowTop + 58f), 7f, 7f, selectionPaint)
        canvas.drawText(item.selectionDetail.selectionTypeName ?: "Selección", 120f, rowTop + 39f, valuePaint)
        canvas.drawText(
            "S/ ${formatMoney(item.totalToPay)}",
            width - 86f - valuePaint.measureText("S/ ${formatMoney(item.totalToPay)}"),
            rowTop + 39f,
            valuePaint
        )

        y += 74f
        drawSummaryLine(canvas, "Peso bruto", "${formatKg(item.grossWeight)} kg", 120f, y, labelPaint, valuePaint)
        drawSummaryLine(canvas, "Jabas", item.crateCount.toString(), 410f, y, labelPaint, valuePaint)
        drawSummaryLine(canvas, "Descuento", "${formatKg(item.grossWeight - item.netWeight)} kg", 610f, y, labelPaint, valuePaint)
        drawSummaryLine(canvas, "Neto", "${formatKg(item.netWeight)} kg", 830f, y, labelPaint, valuePaint)

        y += 54f
        val priceText = item.pricePerKg.toDoubleOrNull()?.let { "S/ ${formatMoney(it)} x kg" } ?: "Sin precio"
        canvas.drawText(priceText, 120f, y, smallPaint)

        if (item.selectionDetail.unitWeights.isNotEmpty()) {
            y += 38f
            canvas.drawText("Pesadas", 120f, y, labelPaint)
            item.selectionDetail.unitWeights.forEachIndexed { index, unit ->
                y += 34f
                val detail = "${index + 1}. ${formatKg(unit.weight)} kg · ${unit.amount} jabas"
                canvas.drawText(detail, 146f, y, smallPaint)
            }
        }

        y += 36f
        canvas.drawLine(86f, y, width - 86f, y, linePaint)
        y += 34f
    }

    val totalRect = RectF(86f, y, width - 86f, y + 104f)
    val totalBg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.rgb(219, 241, 235)
    }
    canvas.drawRoundRect(totalRect, 22f, 22f, totalBg)
    canvas.drawText("Total neto", 122f, y + 43f, labelPaint)
    canvas.drawText("${formatKg(netTotal)} kg", 122f, y + 82f, totalPaint)
    val totalPay = "S/ ${formatMoney(payTotal)}"
    canvas.drawText("Total a pagar", width - 360f, y + 43f, labelPaint)
    canvas.drawText(totalPay, width - 122f - totalPaint.measureText(totalPay), y + 82f, totalPaint)

    return bitmap
}

private fun drawMetric(
    canvas: Canvas,
    rect: RectF,
    label: String,
    value: String,
    labelPaint: Paint,
    valuePaint: Paint
) {
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.rgb(238, 244, 248)
    }
    canvas.drawRoundRect(rect, 18f, 18f, paint)
    canvas.drawText(label, rect.left + 20f, rect.top + 40f, labelPaint)
    canvas.drawText(value, rect.left + 20f, rect.top + 82f, valuePaint)
}

private fun drawSummaryLine(
    canvas: Canvas,
    label: String,
    value: String,
    x: Float,
    y: Float,
    labelPaint: Paint,
    valuePaint: Paint
) {
    canvas.drawText(label, x, y, labelPaint)
    canvas.drawText(value, x, y + 34f, valuePaint)
}

private fun textPaint(size: Float, paintColor: Int, bold: Boolean = false): Paint {
    return Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = paintColor
        textSize = size
        typeface = android.graphics.Typeface.create(
            android.graphics.Typeface.DEFAULT,
            if (bold) android.graphics.Typeface.BOLD else android.graphics.Typeface.NORMAL
        )
    }
}

private fun formatKg(value: Double): String {
    return String.format(Locale.US, "%.2f", value)
}

private fun formatMoney(value: Double): String {
    return String.format(Locale.US, "%.2f", value)
}

private fun formatShareDate(dateStr: String): String {
    return try {
        val date = LocalDate.parse(dateStr.take(10))
        val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
        date.format(formatter)
    } catch (e: Exception) {
        dateStr.take(10)
    }
}
