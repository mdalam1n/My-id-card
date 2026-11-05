package com.example.data.storage

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.media.ExifInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID

class ImageStorageManager(private val context: Context) {

    private val profileDir: File by lazy {
        File(context.filesDir, "profiles").apply { if (!exists()) mkdirs() }
    }

    private val documentsDir: File by lazy {
        File(context.filesDir, "documents").apply { if (!exists()) mkdirs() }
    }

    private val cardsDir: File by lazy {
        File(context.filesDir, "cards").apply { if (!exists()) mkdirs() }
    }

    suspend fun saveProfileImage(uri: Uri, oldPath: String? = null): String? = withContext(Dispatchers.IO) {
        val fileName = "avatar_${System.currentTimeMillis()}.jpg"
        val destination = File(profileDir, fileName)
        val success = processAndSaveImage(uri, destination, maxWidth = 1024, maxHeight = 1024)
        if (success) {
            deleteFileByPath(oldPath)
            destination.absolutePath
        } else {
            null
        }
    }

    suspend fun saveDocumentImage(
        uri: Uri,
        docId: String,
        side: String,
        oldPath: String? = null
    ): String? = withContext(Dispatchers.IO) {
        val fileName = "doc_${docId}_${side}_${System.currentTimeMillis()}.jpg"
        val destination = File(documentsDir, fileName)
        val success = processAndSaveImage(uri, destination, maxWidth = 2560, maxHeight = 2560)
        if (success) {
            deleteFileByPath(oldPath)
            destination.absolutePath
        } else {
            null
        }
    }

    suspend fun saveCardImage(
        uri: Uri,
        cardId: String,
        side: String,
        oldPath: String? = null
    ): String? = withContext(Dispatchers.IO) {
        val fileName = "card_${cardId}_${side}_${System.currentTimeMillis()}.jpg"
        val destination = File(cardsDir, fileName)
        val success = processAndSaveImage(uri, destination, maxWidth = 2560, maxHeight = 2560)
        if (success) {
            deleteFileByPath(oldPath)
            destination.absolutePath
        } else {
            null
        }
    }

    suspend fun deleteFileByPath(path: String?): Boolean = withContext(Dispatchers.IO) {
        if (path.isNullOrBlank()) return@withContext false
        try {
            val file = File(path)
            // Ensure security: only delete if the file is inside this app's filesDir
            if (file.canonicalPath.startsWith(context.filesDir.canonicalPath)) {
                if (file.exists()) {
                    return@withContext file.delete()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        false
    }

    fun doesFileExist(path: String?): Boolean {
        if (path.isNullOrBlank()) return false
        return File(path).exists()
    }

    /**
     * Reads the image from the given Uri, respects EXIF orientation, downsamples
     * safely if massive to prevent OutOfMemory errors, and writes to private internal storage.
     */
    private fun processAndSaveImage(
        uri: Uri,
        destinationFile: File,
        maxWidth: Int,
        maxHeight: Int
    ): Boolean {
        var inputStream: InputStream? = null
        try {
            // First check dimensions
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, options)
            }

            val imageWidth = options.outWidth
            val imageHeight = options.outHeight

            if (imageWidth <= 0 || imageHeight <= 0) {
                // Direct stream copy fallback
                context.contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(destinationFile).use { output ->
                        input.copyTo(output)
                    }
                }
                return destinationFile.exists() && destinationFile.length() > 0
            }

            // Calculate sample size
            var inSampleSize = 1
            while (imageWidth / (inSampleSize * 2) >= maxWidth || imageHeight / (inSampleSize * 2) >= maxHeight) {
                inSampleSize *= 2
            }

            val decodeOptions = BitmapFactory.Options().apply {
                this.inSampleSize = inSampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }

            val decodedBitmap = context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, decodeOptions)
            } ?: return false

            // Check orientation from EXIF
            var rotationAngle = 0f
            try {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val exif = ExifInterface(stream)
                    val orientation = exif.getAttributeInt(
                        ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_NORMAL
                    )
                    rotationAngle = when (orientation) {
                        ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                        ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                        ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                        else -> 0f
                    }
                }
            } catch (ignored: Exception) {
            }

            val finalBitmap = if (rotationAngle != 0f) {
                val matrix = Matrix().apply { postRotate(rotationAngle) }
                val rotated = Bitmap.createBitmap(
                    decodedBitmap, 0, 0,
                    decodedBitmap.width, decodedBitmap.height,
                    matrix, true
                )
                if (rotated != decodedBitmap) {
                    decodedBitmap.recycle()
                }
                rotated
            } else {
                decodedBitmap
            }

            FileOutputStream(destinationFile).use { outStream ->
                finalBitmap.compress(Bitmap.CompressFormat.JPEG, 92, outStream)
            }
            finalBitmap.recycle()

            return destinationFile.exists() && destinationFile.length() > 0
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback raw copy
            try {
                context.contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(destinationFile).use { output ->
                        input.copyTo(output)
                    }
                }
                return destinationFile.exists() && destinationFile.length() > 0
            } catch (fallbackError: Exception) {
                fallbackError.printStackTrace()
                return false
            }
        } finally {
            try {
                inputStream?.close()
            } catch (ignored: Exception) {}
        }
    }
}
