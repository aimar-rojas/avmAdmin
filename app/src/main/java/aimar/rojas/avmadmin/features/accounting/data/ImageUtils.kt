package aimar.rojas.avmadmin.features.accounting.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

object ImageUtils {

    fun compressAndSaveToWebp(context: Context, imageUri: Uri): File {
        val inputStream: InputStream? = context.contentResolver.openInputStream(imageUri)
        val originalBitmap = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()

        // Ajustar orientación según EXIF si existe
        val rotatedBitmap = rotateBitmapIfRequired(context, imageUri, originalBitmap)

        // Redimensionar si es muy grande (máximo 1920px de ancho/alto)
        val scaledBitmap = scaleBitmapPreservingRatio(rotatedBitmap, 1920)

        val outputFile = File(context.cacheDir, "invoice_${System.currentTimeMillis()}.webp")
        val outputStream = FileOutputStream(outputFile)

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            scaledBitmap.compress(Bitmap.CompressFormat.WEBP_LOSSY, 85, outputStream)
        } else {
            @Suppress("DEPRECATION")
            scaledBitmap.compress(Bitmap.CompressFormat.WEBP, 85, outputStream)
        }

        outputStream.flush()
        outputStream.close()

        if (scaledBitmap != originalBitmap) {
            scaledBitmap.recycle()
        }
        rotatedBitmap.recycle()

        return outputFile
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
