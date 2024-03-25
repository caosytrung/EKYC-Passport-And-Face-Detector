package com.fast.ekyc.utils

import android.graphics.Bitmap
import android.graphics.Rect
import com.fast.ekyc.base.ui.camera.CustomSize
import kotlin.math.roundToInt
import kotlin.math.sqrt

internal object BitmapUtils {
    private const val MAX_IMAGE_RESOLUTION = 1_200_000

    internal fun Bitmap.scaled(ratio: Float, filter: Boolean) = if (ratio != 1f) {
        Bitmap.createScaledBitmap(
            this,
            (width * ratio).roundToInt(),
            (height * ratio).roundToInt(),
            filter
        )
    } else {
        this
    }

    fun cropBitmap(windowSize: CustomSize, hole: Rect, originalBitmap: Bitmap): Bitmap? {
        val bitmapPicture = originalBitmap.let {
            val size = originalBitmap.width * originalBitmap.height
            if (size <= MAX_IMAGE_RESOLUTION) {
                 originalBitmap
            } else {
                originalBitmap.scaled(sqrt(MAX_IMAGE_RESOLUTION / size.toFloat()), true)
            }

        }

        //calculate aspect ratio
        val withRatio: Float =
            bitmapPicture.width.toFloat() / windowSize.width.toFloat()
        val heightRatio: Float =
            bitmapPicture.height.toFloat() / windowSize.height.toFloat()

        //get viewfinder border size and position on the screen
        val x1 = hole.left
        val y1 = hole.top
        val x2 = hole.width()
        val y2 = hole.height()
        //calculate position and size for cropping
        val cropStartX = (x1 * withRatio).roundToInt()
        val cropStartY = (y1 * heightRatio).roundToInt()
        val cropWidthX = (x2 * withRatio).roundToInt()
        val cropHeightY = (y2 * heightRatio).roundToInt()

        //check limits and make crop

        return if (cropStartX + cropWidthX <= bitmapPicture.width && cropStartY + cropHeightY <= bitmapPicture.height) {
            Bitmap.createBitmap(
                bitmapPicture,
                cropStartX,
                cropStartY,
                cropWidthX,
                cropHeightY
            )
        } else {
            null
        }
    }
}