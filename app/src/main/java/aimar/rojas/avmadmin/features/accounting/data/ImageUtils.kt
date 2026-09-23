package aimar.rojas.avmadmin.features.accounting.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

object ImageUtils {

    /**
     * Verifica si una URI corresponde a un archivo PDF.
     */
    fun isPdfUri(context: Context, uri: Uri): Boolean {
        val mimeType = context.contentResolver.getType(uri)
        if (mimeType != null && mimeType.contains("pdf", ignoreCase = true)) {
            return true
        }
        val path = uri.path?.lowercase() ?: ""
        return path.endsWith(".pdf")
    }

    /**
     * Copia un archivo PDF a cacheDir para subirlo de forma íntegra al backend.
     */
    fun copyPdfToCache(context: Context, pdfUri: Uri): File {
        val outputFile = File(context.cacheDir, "invoice_${System.currentTimeMillis()}.pdf")
        context.contentResolver.openInputStream(pdfUri)?.use { input ->
            FileOutputStream(outputFile).use { output ->
                input.copyTo(output)
            }
        } ?: throw IllegalArgumentException("No se pudo leer el archivo PDF")
        return outputFile
    }

    /**
     * Renderiza la primera página de un documento PDF a un Bitmap nítido para OCR y previsualización.
     */
    fun renderPdfFirstPageToBitmap(context: Context, pdfUri: Uri): Bitmap {
        val pfd: ParcelFileDescriptor = context.contentResolver.openFileDescriptor(pdfUri, "r")
            ?: throw IllegalArgumentException("No se pudo abrir el descriptor del archivo PDF")

        try {
            val renderer = PdfRenderer(pfd)
            if (renderer.pageCount == 0) {
                renderer.close()
                throw IllegalArgumentException("El archivo PDF no contiene páginas")
            }

            val page = renderer.openPage(0)
            val scale = 2.0f
            val width = (page.width * scale).toInt()
            val height = (page.height * scale).toInt()

            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            canvas.drawColor(Color.WHITE)

            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            page.close()
            renderer.close()

            return bitmap
        } finally {
            try {
                pfd.close()
            } catch (_: Exception) {}
        }
    }

    /**
     * Renderiza la primera página de un PDF y la guarda como WebP para previsualización o análisis.
     */
    fun renderPdfFirstPageToWebp(context: Context, pdfUri: Uri): File {
        val bitmap = renderPdfFirstPageToBitmap(context, pdfUri)
        val outputFile = File(context.cacheDir, "invoice_preview_${System.currentTimeMillis()}.webp")
        val outputStream = FileOutputStream(outputFile)

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            bitmap.compress(Bitmap.CompressFormat.WEBP_LOSSY, 85, outputStream)
        } else {
            @Suppress("DEPRECATION")
            bitmap.compress(Bitmap.CompressFormat.WEBP, 85, outputStream)
        }

        outputStream.flush()
        outputStream.close()
        bitmap.recycle()

        return outputFile
    }

    /**
     * Procesa, optimiza y comprime la imagen escaneada:
     * 1. Corrige orientación EXIF.
     * 2. Escala a resolución óptima (máx. 1920px).
     * 3. Aplica realce inteligente adaptativo de exposición y contraste (reducción de sobreexposición y realce de tinta).
     * 4. Guarda en formato WebP en cacheDir.
     */
    fun compressAndSaveToWebp(
        context: Context,
        imageUri: Uri,
        enableSmartEnhancement: Boolean = true
    ): File {
        val inputStream: InputStream? = context.contentResolver.openInputStream(imageUri)
        val originalBitmap = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()

        if (originalBitmap == null) {
            throw IllegalArgumentException("No se pudo decodificar el bitmap desde la URI proporcionada")
        }

        // 1. Ajustar orientación según EXIF si existe
        val rotatedBitmap = rotateBitmapIfRequired(context, imageUri, originalBitmap)

        // 2. Redimensionar si es muy grande (máximo 1920px de ancho/alto)
        val scaledBitmap = scaleBitmapPreservingRatio(rotatedBitmap, 1920)

        // 3. Aplicar realce inteligente de contraste y compensación de exposición
        val enhancedBitmap = if (enableSmartEnhancement) {
            applySmartDocumentEnhancement(scaledBitmap)
        } else {
            scaledBitmap
        }

        val outputFile = File(context.cacheDir, "invoice_${System.currentTimeMillis()}.webp")
        val outputStream = FileOutputStream(outputFile)

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            enhancedBitmap.compress(Bitmap.CompressFormat.WEBP_LOSSY, 85, outputStream)
        } else {
            @Suppress("DEPRECATION")
            enhancedBitmap.compress(Bitmap.CompressFormat.WEBP, 85, outputStream)
        }

        outputStream.flush()
        outputStream.close()

        // Liberar memoria
        if (enhancedBitmap != scaledBitmap && enhancedBitmap != rotatedBitmap && enhancedBitmap != originalBitmap) {
            enhancedBitmap.recycle()
        }
        if (scaledBitmap != rotatedBitmap && scaledBitmap != originalBitmap) {
            scaledBitmap.recycle()
        }
        if (rotatedBitmap != originalBitmap) {
            rotatedBitmap.recycle()
        }
        originalBitmap.recycle()

        return outputFile
    }

    /**
     * Analiza la luminosidad de la imagen y aplica una transformación de matriz de color
     * adaptada a documentos contables (papel térmico, hojas A4, boletas con tinta tenue o sobreexpuestas).
     */
    private fun applySmartDocumentEnhancement(bitmap: Bitmap): Bitmap {
        val stats = analyzeLuminance(bitmap)

        // Determinar contraste, compensación de brillo y saturación según histograma
        val contrast: Float
        val brightnessOffset: Float
        val saturation: Float

        when {
            // Escenario 1: Imagen muy sobreexpuesta (mucha luz/flash/reflejo en papel blanco)
            stats.avgLuminance > 175f && stats.highLuminanceRatio > 0.35f -> {
                contrast = 1.38f
                brightnessOffset = -22f // Reduce altas luces y oscurece la tinta lavada
                saturation = 0.75f // Reduce aberración cromática
            }
            // Escenario 2: Imagen oscura / baja iluminación (tomada en almacén o sombra)
            stats.avgLuminance < 115f -> {
                contrast = 1.25f
                brightnessOffset = 20f // Ilumina el papel y resalta letras
                saturation = 0.85f
            }
            // Escenario 3: Exposición normal, realce estándar de legibilidad
            else -> {
                contrast = 1.22f
                brightnessOffset = -6f // Texto más nítido y fondo blanco uniforme
                saturation = 0.85f
            }
        }

        // Construir ColorMatrix de contraste y brillo
        val scale = contrast
        val translate = (-0.5f * scale + 0.5f) * 255f + brightnessOffset

        val contrastMatrix = ColorMatrix(floatArrayOf(
            scale, 0f, 0f, 0f, translate,
            0f, scale, 0f, 0f, translate,
            0f, 0f, scale, 0f, translate,
            0f, 0f, 0f, 1f, 0f
        ))

        // Ajustar saturación para limpiar ruido de color
        val satMatrix = ColorMatrix().apply { setSaturation(saturation) }
        contrastMatrix.preConcat(satMatrix)

        // Renderizar el bitmap mejorado mediante aceleración gráfica de Canvas/Skia
        val enhancedBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(enhancedBitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
            colorFilter = ColorMatrixColorFilter(contrastMatrix)
        }
        canvas.drawBitmap(bitmap, 0f, 0f, paint)

        return enhancedBitmap
    }

    private data class LuminanceStats(
        val avgLuminance: Float,
        val highLuminanceRatio: Float,
        val lowLuminanceRatio: Float
    )

    /**
     * Muestrea una cuadrícula reducida de 64x64 píxeles para calcular estadísticas en < 2ms
     */
    private fun analyzeLuminance(bitmap: Bitmap): LuminanceStats {
        val sampleSize = 64
        val scaled = Bitmap.createScaledBitmap(bitmap, sampleSize, sampleSize, false)
        val pixels = IntArray(sampleSize * sampleSize)
        scaled.getPixels(pixels, 0, sampleSize, 0, 0, sampleSize, sampleSize)
        if (scaled != bitmap) {
            scaled.recycle()
        }

        var totalLum = 0.0
        var highCount = 0
        var lowCount = 0

        for (pixel in pixels) {
            val r = (pixel shr 16) and 0xFF
            val g = (pixel shr 8) and 0xFF
            val b = pixel and 0xFF
            // Fórmula estándar ITU-R BT.601 para luminancia
            val lum = 0.299f * r + 0.587f * g + 0.114f * b
            totalLum += lum
            if (lum > 220f) highCount++
            if (lum < 75f) lowCount++
        }

        val count = pixels.size.toFloat()
        return LuminanceStats(
            avgLuminance = (totalLum / count).toFloat(),
            highLuminanceRatio = highCount / count,
            lowLuminanceRatio = lowCount / count
        )
    }

    private fun scaleBitmapPreservingRatio(bitmap: Bitmap, maxDimension: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        if (width <= maxDimension && height <= maxDimension) {
            return bitmap
        }

        val ratio = width.toFloat() / height.toFloat()
        val newWidth: Int
        val newHeight: Int

        if (width > height) {
            newWidth = maxDimension
            newHeight = (maxDimension / ratio).toInt()
        } else {
            newHeight = maxDimension
            newWidth = (maxDimension * ratio).toInt()
        }

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    private fun rotateBitmapIfRequired(context: Context, imageUri: Uri, bitmap: Bitmap): Bitmap {
        return try {
            val input = context.contentResolver.openInputStream(imageUri) ?: return bitmap
            val exif = ExifInterface(input)
            val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
            input.close()

            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> rotateBitmap(bitmap, 90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> rotateBitmap(bitmap, 180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> rotateBitmap(bitmap, 270f)
                else -> bitmap
            }
        } catch (e: Exception) {
            bitmap
        }
    }

    private fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degrees)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
}
