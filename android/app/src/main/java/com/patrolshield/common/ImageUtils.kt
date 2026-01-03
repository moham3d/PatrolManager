package com.patrolshield.common

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

object ImageUtils {

    fun compressImage(uri: Uri, context: Context): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            val outputFile = File(context.cacheDir, "compressed_${System.currentTimeMillis()}.jpg")
            var quality = 80
            var stream = FileOutputStream(outputFile)
            
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
            stream.flush()
            stream.close()

            // Iterative scaling if still over 5MB
            var currentFile = outputFile
            while (currentFile.length() > 5 * 1024 * 1024 && quality > 10) {
                quality -= 10
                stream = FileOutputStream(outputFile)
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
                stream.flush()
                stream.close()
                currentFile = outputFile
            }

            currentFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
