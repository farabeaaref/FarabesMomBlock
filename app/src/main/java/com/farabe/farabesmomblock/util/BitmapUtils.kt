package com.farabe.farabesmomblock.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.io.InputStream

/**
 * Decodes local image files, content URIs, and asset streams safely with
 * EXIF orientation correction and bounds-safe downsampling so vertical phone
 * photos (e.g., iPhone/Android portraits) always display upright without OOM.
 */
object BitmapUtils {

    fun decodeBitmapFileWithExif(file: File, maxDim: Int = 1920): Bitmap? {
        if (!file.exists() || file.length() <= 0L) return null
        return try {
            val orientation = try {
                ExifInterface(file.absolutePath).getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )
            } catch (_: Exception) {
                ExifInterface.ORIENTATION_NORMAL
            }

            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(file.absolutePath, bounds)

            val sampleSize = computeInSampleSize(bounds.outWidth, bounds.outHeight, maxDim)
            val opts = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
            val raw = BitmapFactory.decodeFile(file.absolutePath, opts) ?: return null
            applyExifTransform(raw, orientation)
        } catch (_: Exception) {
            null
        }
    }

    fun decodeUriWithExif(context: Context, uri: Uri, maxDim: Int = 1920): Bitmap? {
        return try {
            val orientation = try {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    ExifInterface(stream).getAttributeInt(
                        ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_NORMAL
                    )
                } ?: ExifInterface.ORIENTATION_NORMAL
            } catch (_: Exception) {
                ExifInterface.ORIENTATION_NORMAL
            }

            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, bounds)
            }

            val sampleSize = computeInSampleSize(bounds.outWidth, bounds.outHeight, maxDim)
            val opts = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
            val raw = context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, opts)
            } ?: return null

            applyExifTransform(raw, orientation)
        } catch (_: Exception) {
            null
        }
    }

    fun decodeAssetWithExif(context: Context, assetPath: String, maxDim: Int = 1920): Bitmap? {
        return try {
            val orientation = try {
                context.assets.open(assetPath).use { stream ->
                    ExifInterface(stream).getAttributeInt(
                        ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_NORMAL
                    )
                }
            } catch (_: Exception) {
                ExifInterface.ORIENTATION_NORMAL
            }

            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.assets.open(assetPath).use { stream ->
                BitmapFactory.decodeStream(stream, null, bounds)
            }

            val sampleSize = computeInSampleSize(bounds.outWidth, bounds.outHeight, maxDim)
            val opts = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
            val raw = context.assets.open(assetPath).use { stream ->
                BitmapFactory.decodeStream(stream, null, opts)
            } ?: return null

            applyExifTransform(raw, orientation)
        } catch (_: Exception) {
            null
        }
    }

    private fun computeInSampleSize(width: Int, height: Int, maxDim: Int): Int {
        var inSampleSize = 1
        if (width <= 0 || height <= 0) return 1
        var halfW = width
        var halfH = height
        while (halfW > maxDim || halfH > maxDim) {
            halfW /= 2
            halfH /= 2
            inSampleSize *= 2
        }
        return inSampleSize.coerceAtLeast(1)
    }

    private fun applyExifTransform(bitmap: Bitmap, orientation: Int): Bitmap {
        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.preScale(-1f, 1f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.preScale(1f, -1f)
            ExifInterface.ORIENTATION_TRANSPOSE -> {
                matrix.postRotate(90f)
                matrix.preScale(-1f, 1f)
            }
            ExifInterface.ORIENTATION_TRANSVERSE -> {
                matrix.postRotate(270f)
                matrix.preScale(-1f, 1f)
            }
            else -> return bitmap
        }
        return try {
            val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            if (rotated != bitmap) {
                bitmap.recycle()
            }
            rotated
        } catch (_: Exception) {
            bitmap
        }
    }
}
