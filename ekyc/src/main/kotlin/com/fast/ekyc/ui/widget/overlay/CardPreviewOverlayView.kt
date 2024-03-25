package com.fast.ekyc.ui.widget.overlay

import android.content.Context
import android.graphics.*
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.graphics.toRectF
import com.fast.ekyc.R
import com.fast.ekyc.utils.extension.getDimen

internal class CardPreviewOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val frameHorizontalMargin = getDimen(R.dimen.kyc_margin_padding_default)
    private val frameThickness = getDimen(R.dimen.kyc_overlay_preview_frame_thickness)
    private val circleRadius = getDimen(R.dimen.kyc_overlay_preview_circle_radius).toFloat()

    private val framePaint = Paint().apply {
        style = Paint.Style.STROKE
        color = ContextCompat.getColor(context, R.color.kyc_color_white)
        strokeWidth = frameThickness.toFloat()
        isAntiAlias = true
    }
    private val circlePaint = Paint().apply {
        style = Paint.Style.FILL
        color = ContextCompat.getColor(context, R.color.kyc_color_white)
        strokeWidth = 0f
        isAntiAlias
    }
    private val holePaint = Paint().apply {
        color = Color.TRANSPARENT
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    }

    private val overlayColor = ContextCompat.getColor(context, R.color.kyc_overlay_color)
    private var isFrameVisible: Boolean = false

    init {
        setWillNotDraw(false)
        setLayerType(LAYER_TYPE_HARDWARE, null)


        Handler(Looper.getMainLooper()).postDelayed({
            isFrameVisible = true
            invalidate()
        }, 100)
    }

    override fun onDraw(canvas: Canvas) {
        val hole = getHole()

        canvas.drawColor(overlayColor)
        canvas.save()

        if (isFrameVisible) {
            // draw hole
            canvas.drawRect(hole.toRectF(), holePaint)
            canvas.drawRect(hole.toRectF(), framePaint)
            canvas.save()

            // draw circles
//            drawFrame(canvas, hole)
        }
    }

    private fun drawFrame(canvas: Canvas, hole: Rect) {
        val holdF = hole.toRectF()

        canvas.drawCircle(holdF.left, holdF.bottom, circleRadius, circlePaint)
        canvas.drawCircle(holdF.right, holdF.bottom, circleRadius, circlePaint)
        canvas.drawCircle(holdF.right, holdF.top, circleRadius, circlePaint)
        canvas.drawCircle(holdF.left, holdF.top, circleRadius, circlePaint)
    }

    fun setFrameVisible(isVisible: Boolean) {
        isFrameVisible = isVisible
        invalidate()
    }

    fun getHole(): Rect {
        val screenWidthFloat = width
        val screenHeightFloat = height

        val width = screenWidthFloat - frameHorizontalMargin * 2
        val height = (width / CardCameraOverlayView.CARD_RATIO).toInt()
        val left = frameHorizontalMargin
        val bottom = (height + screenHeightFloat) / 2

        return Rect(
            left,
            bottom - height,
            left + width,
            bottom
        )
    }

    fun getCroppingHole(): Rect {
        val originalHold = getHole()
        val deltaX = minOf((originalHold.width() * 0.1f).toInt(), originalHold.left)
        val deltaY = (originalHold.height() * 0.1f).toInt()
        val newTop = originalHold.top - deltaY
        val newBottom = originalHold.bottom + deltaY
        val newLeft = originalHold.left - deltaX
        val newRight = originalHold.right + deltaX

        return Rect(newLeft, newTop, newRight, newBottom)
    }

}