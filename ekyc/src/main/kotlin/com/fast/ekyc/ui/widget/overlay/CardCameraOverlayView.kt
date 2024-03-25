package com.fast.ekyc.ui.widget.overlay

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.graphics.toRectF
import com.fast.ekyc.R
import com.fast.ekyc.base.ui.camera.CustomSize
import com.fast.ekyc.utils.extension.getDimen

internal class CardCameraOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val frameHorizontalMargin = getDimen(R.dimen.kyc_margin_padding_default)
    private val frameRadius = getDimen(R.dimen.kyc_radius_default).toFloat()
    private var isFrameVisible: Boolean = true

    private val borderColor = ContextCompat.getColor(context, R.color.kyc_color_white)
    private val borderThickness = getDimen(R.dimen.kyc_overlay_preview_frame_thickness)

    private val holePaint = Paint().apply {
        color = Color.TRANSPARENT
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    }

    private var debugOriginalFaceBox: RectF? = null
    private var debugUseFaceBox: RectF? = null

    private val debugFaceBoxPaint = Paint().apply {
        style = Paint.Style.STROKE
        color = ContextCompat.getColor(context, R.color.kyc_color_error)
        strokeWidth = borderThickness.toFloat()
        isAntiAlias = true
    }

    private val debugUseFaceBoxPaint = Paint().apply {
        style = Paint.Style.STROKE
        color = ContextCompat.getColor(context, R.color.kyc_color_green)
        strokeWidth = borderThickness.toFloat()
        isAntiAlias = true
    }

    private val borderPaint = Paint().apply {
        style = Paint.Style.STROKE
        color = borderColor
        strokeWidth = borderThickness.toFloat()
        isAntiAlias = true
    }

    private val overlayColor = ContextCompat.getColor(context, R.color.kyc_overlay_color)

    init {
        setWillNotDraw(false)
        setLayerType(LAYER_TYPE_HARDWARE, null)
    }

    override fun onDraw(canvas: Canvas) {
        val hole = getHole()
        canvas.drawColor(overlayColor)
        canvas.save()

        if (isFrameVisible) {
            // draw hole
            canvas.drawRoundRect(hole.toRectF(), frameRadius, frameRadius, holePaint)
            canvas.drawRoundRect(hole.toRectF(), frameRadius, frameRadius, borderPaint)
            canvas.save()
        }

        if (debugOriginalFaceBox != null) {
            canvas.drawRect(debugOriginalFaceBox!!, debugFaceBoxPaint)
        }

        if (debugUseFaceBox != null) {
            canvas.drawRect(debugUseFaceBox!!, debugUseFaceBoxPaint)
        }
    }

    fun setFrameVisible(isVisible: Boolean) {
        isFrameVisible = isVisible
        invalidate()
    }

    fun getRatioOfHole(): RectF {
        val screenWidthFloat = width.toFloat()
        val screenHeightFloat = height.toFloat()
        val hole = getHole()
        return RectF(
            hole.left / screenWidthFloat,
            hole.top / screenHeightFloat,
            hole.right / screenWidthFloat,
            hole.bottom / screenHeightFloat
        )
    }

    fun getHole(): Rect {
        val screenWidthFloat = width
        val screenHeightFloat = height

        val width = screenWidthFloat - frameHorizontalMargin * 2
        val height = (width / CARD_RATIO).toInt()
        val left = frameHorizontalMargin
        val bottom = (height + screenHeightFloat) / 2

        return Rect(
            left,
            bottom - height,
            left + width,
            bottom
        )
    }

    fun drawCardBox(card: RectF, screenSizeByBitmap: CustomSize, useCardRatio: Float) {
        val screenWidth = width
        val screenHeight = height

        val screenSizeBytBitmapWidth = screenSizeByBitmap.width
        val screenSizeBytBitmapHeight = screenSizeByBitmap.height

        val left = card.left * screenWidth / screenSizeBytBitmapWidth
        val right = card.right * screenWidth / screenSizeBytBitmapWidth
        val top = card.top * screenHeight / screenSizeBytBitmapHeight
        val bottom = card.bottom * screenHeight / screenSizeBytBitmapHeight
        debugOriginalFaceBox = RectF(left, top, right, bottom)

        val useLeft =
            (card.left + card.width() * useCardRatio) * screenWidth / screenSizeBytBitmapWidth
        val useRight =
            (card.right - card.width() * useCardRatio) * screenWidth / screenSizeBytBitmapWidth
        val useTop =
            (card.top + card.height() * useCardRatio) * screenHeight / screenSizeBytBitmapHeight
        val useBottom =
            (card.bottom - card.height() * useCardRatio) * screenHeight / screenSizeBytBitmapHeight

        debugUseFaceBox = RectF(useLeft, useTop, useRight, useBottom)

        invalidate()
    }

    companion object {
        const val CARD_RATIO = 1.58
    }
}