package com.fast.ekyc.ui.widget.overlay

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.graphics.toRectF
import com.fast.ekyc.R
import com.fast.ekyc.base.ui.camera.CustomSize
import com.fast.ekyc.ui.state.CaptureState
import com.fast.ekyc.utils.extension.getDimen


internal class FaceCameraOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val startColor = Color.parseColor("#34BE75")
    private val endColor = Color.parseColor("#22321f")

    private val backgroundColor = ContextCompat.getColor(context, R.color.kyc_face_overlay_color)
    private val topHeight = getDimen(R.dimen.kyc_margin_padding_default_smaller)
    private val bottomHeight = getDimen(R.dimen.kyc_margin_padding_default_smaller)

    private val marginLeft = getDimen(R.dimen.kyc_margin_padding_default)

    private val frameThickness = getDimen(R.dimen.kyc_card_overlay_preview_circle_radius).toFloat()
    private val smallThickness = getDimen(R.dimen.kyc_overlay_preview_circle_radius).toFloat()
    private val frameSize = getDimen(R.dimen.kyc_overlay_item_frame_size)
    private val frameRadius = getDimen(R.dimen.kyc_radius_default).toFloat()
    val options = BitmapFactory.Options().apply {
        inSampleSize = 8
    }
    private val backgroundColorPaint = Paint().apply {
        maskFilter = BlurMaskFilter(8f, BlurMaskFilter.Blur.NORMAL)
    }

    private val framePaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = smallThickness
        isAntiAlias = true
    }

    private val holeBorderPaint = Paint().apply {
        style = Paint.Style.STROKE
        color = ContextCompat.getColor(context, R.color.kyc_color_white)
        strokeWidth = frameThickness
        isAntiAlias = true
    }

    private val progressPaint = Paint().apply {
        style = Paint.Style.STROKE
        isAntiAlias = true
        strokeWidth = frameThickness
    }

    private val holePaint = Paint().apply {
        color = Color.TRANSPARENT
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    }

    private val debugFaceBoxPaint = Paint().apply {
        style = Paint.Style.STROKE
        color = ContextCompat.getColor(context, R.color.kyc_color_green)
        strokeWidth = smallThickness
        isAntiAlias = true
    }

    private var isFaceAdvanced: Boolean = true
    private var isDrawAdvancedProgress: Boolean = false
    private var faceAdvancedPercent = 0


    private var debugOriginalFaceBox: RectF? = null

    init {
        setWillNotDraw(false)
        setLayerType(LAYER_TYPE_HARDWARE, null)
        setNormalFaceStateColor(CaptureState.UNKNOWN)
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawColor(backgroundColor)

        drawHole(canvas)

        if (debugOriginalFaceBox != null) {
            canvas.drawRect(debugOriginalFaceBox!!, debugFaceBoxPaint)
        }

        canvas.save()
    }

    fun setFaceAdvanced(isAdvanced: Boolean) {
        isFaceAdvanced = isAdvanced
        invalidate()
    }

    private fun drawHole(canvas: Canvas) {
        val hole = getHole() ?: return

        // draw hole
        canvas.drawOval(
            hole.toRectF(),
            holePaint
        )

        // draw border of hole
        canvas.drawOval(
            hole.toRectF(),
            holeBorderPaint
        )

        if (isFaceAdvanced) {
            if (isDrawAdvancedProgress) {
                progressPaint.shader = LinearGradient(
                    0f, 0f, 0f,
                    hole.height().toFloat(), startColor, endColor, Shader.TileMode.MIRROR
                )
                val sweepAngle = faceAdvancedPercent * 360 / 100f
                canvas.drawArc(hole.toRectF(), -90f, -sweepAngle, false, progressPaint)
            }
        } else {
            drawFrame(canvas, hole)
        }
    }

    private var cacheHole: Rect? = null
    fun getHole(): Rect? {

        if (width <= 0 || height <= 0) {
            return null
        }

        val maxWidth = width - marginLeft * 2f
        val maxHeight = height.toFloat() - bottomHeight - topHeight

        if (cacheHole != null) {
            return cacheHole
        }

        if (maxHeight / maxWidth > FRAME_RATIO) {
            val holeWidth = maxWidth.toInt()
            val holdHeight = (maxWidth * FRAME_RATIO).toInt()
            val holdTop = (height - bottomHeight - topHeight - holdHeight) / 2 + topHeight
            cacheHole = Rect(marginLeft, holdTop, marginLeft + holeWidth, holdTop + holdHeight)
        } else {
            val holdHeight = maxHeight.toInt()
            val holeWidth = (holdHeight / FRAME_RATIO).toInt()
            val holeLeft = (width - holeWidth) / 2
            cacheHole = Rect(holeLeft, topHeight, holeLeft + holeWidth, topHeight + holdHeight)
        }

        return cacheHole
    }

    fun getCroppingHole(): Rect {
        return getHole() ?: return Rect()
//        val deltaX = minOf((originalHold.width() * 0.1f).toInt(), originalHold.left)
//        val deltaY = (originalHold.height() * 0.1f).toInt()
//        val newTop = originalHold.top - deltaY
//        val newBottom = originalHold.bottom + deltaY
//        val newLeft = originalHold.left - deltaX
//        val newRight = originalHold.right + deltaX
//
//        return Rect(newLeft, newTop, newRight, newBottom)
    }

    fun getRatioOfHole(): RectF? {
        val screenWidthFloat = width.toFloat()
        val screenHeightFloat = height.toFloat()

        return getHole()?.let {
            RectF(
                it.left / screenWidthFloat,
                it.top / screenHeightFloat,
                it.right / screenWidthFloat,
                it.bottom / screenHeightFloat
            )
        }
    }

    private fun drawFrame(canvas: Canvas, hole: Rect) {
        val frameMargin = 0
        val path = Path()
        val deltaX = frameThickness / 2f + frameMargin
        val deltaY = (frameSize - frameThickness).toFloat() - frameMargin
        val realFrameSize = frameSize - frameThickness / 2f

        // top-left
        path.moveTo(hole.left - deltaX, hole.top + deltaY)
        path.rLineTo(0f, frameRadius - realFrameSize)
        path.rQuadTo(0f, -frameRadius, frameRadius, -frameRadius)
        path.rLineTo(realFrameSize - frameRadius, 0f)

        // top-right
        path.moveTo(hole.right + deltaX, hole.top + deltaY)
        path.rLineTo(0f, -realFrameSize + frameRadius)
        path.rQuadTo(0f, -frameRadius, -frameRadius, -frameRadius)
        path.rLineTo(frameRadius - realFrameSize, 0f)

        // bottom-right
        path.moveTo(hole.right + deltaX, hole.bottom - deltaY)
        path.rLineTo(0f, realFrameSize - frameRadius)
        path.rQuadTo(0f, frameRadius, -frameRadius, frameRadius)
        path.rLineTo(-realFrameSize + frameRadius, 0f)

        // bottom-left
        path.moveTo(hole.left - deltaX, hole.bottom - deltaY)
        path.rLineTo(0f, realFrameSize - frameRadius)
        path.rQuadTo(0f, frameRadius, frameRadius, frameRadius)
        path.rLineTo(realFrameSize - frameRadius, 0f)

        canvas.drawPath(path, framePaint)
        canvas.save()
    }

    fun setNormalFaceStateColor(
        faceState: CaptureState,
    ) {
        if (isFaceAdvanced) {
            return
        }

        if (faceState.isValid()) {
            framePaint.color = ContextCompat.getColor(context, R.color.kyc_color_green)
        } else {
            framePaint.color = Color.TRANSPARENT
        }

        invalidate()
    }

    fun setAdvancedFaceState(
        isRecording: Boolean,
        percentage: Int
    ) {
        if (!isFaceAdvanced) {
            return
        }

        isDrawAdvancedProgress = isRecording

        faceAdvancedPercent = if (isRecording) {
            percentage
        } else {
            0
        }

        invalidate()
    }

    fun drawFaceBox(faceBox: RectF, screenSizeByBitmap: CustomSize) {
        val screenWidth = width
        val screenHeight = height

        val screenSizeBytBitmapWidth = screenSizeByBitmap.width
        val screenSizeBytBitmapHeight = screenSizeByBitmap.height

        val left = faceBox.left * screenWidth / screenSizeBytBitmapWidth
        val right = faceBox.right * screenWidth / screenSizeBytBitmapWidth
        val top = faceBox.top * screenHeight / screenSizeBytBitmapHeight
        val bottom = faceBox.bottom * screenHeight / screenSizeBytBitmapHeight
        debugOriginalFaceBox = RectF(left, top, right, bottom)

        invalidate()
    }

    companion object {
        const val FRAME_RATIO = 1.25f
    }
}