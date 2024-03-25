package com.fast.ekyc.utils

import android.content.Context
import android.content.res.AssetManager
import android.graphics.Bitmap
import java.io.*

internal object FileUtils {
    private const val MATRIX_FILE="matrix.yml"
    private const val KYC_DIR="kycImages"

    private fun bitmapToFile(bitmap: Bitmap, file: File) { // File name like "image.png"
        return try {
            //Convert bitmap to byte array
            val bos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos) // YOU can also save it in JPEG
            val bitmapData = bos.toByteArray()

            //write the bytes in file
            val fos = FileOutputStream(file)
            fos.write(bitmapData)
            fos.flush()
            fos.close()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    private fun createNewYmlFile(context: Context): Pair<File,Boolean> {
        return context.let {
            val file = File(it.filesDir, KYC_DIR)
              if (!file.exists()) {
                file.mkdir()
                File(file, MATRIX_FILE) to false
            } else File(file, MATRIX_FILE) to true

        }
    }

    @Throws(IOException::class)
    private fun copyFile(`in`: InputStream?, out: OutputStream) {
        val buffer = ByteArray(1024)
        var read: Int? = null
        while (`in`?.read(buffer).also { read = it!! } != -1) {
            read?.let { out.write(buffer, 0, it) }
        }
    }

    fun copyAssets(context: Context): String? {
        val outFilePair = createNewYmlFile(context)
        val assetManager: AssetManager = context.assets

        var isStream: InputStream? = null
        var osStream: OutputStream?  = null
        return try {
            val outFile = outFilePair.first
            isStream = assetManager.open(MATRIX_FILE)
            osStream = FileOutputStream(outFile)
            copyFile(isStream, osStream)
            outFile.path

        } catch (e: IOException) {
            null
        } finally {
            if (isStream != null) {
                try {
                    isStream.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            if (osStream != null) {
                try {
                    osStream.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }
}