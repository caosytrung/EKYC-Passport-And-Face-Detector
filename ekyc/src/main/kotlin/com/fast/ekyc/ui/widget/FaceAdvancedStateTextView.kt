package com.fast.ekyc.ui.widget

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import androidx.core.content.ContextCompat
import com.fast.ekyc.R
import com.fast.ekyc.theme.ThemeHolder
import com.fast.ekyc.ui.state.CaptureState
import com.fast.ekyc.utils.ShapeBuilder
import java.util.*
import kotlin.concurrent.schedule

internal class FaceAdvancedStateTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = android.R.attr.textViewStyle
) : AppRegularTextView(context, attrs, defStyleAttr) {

    private val backgroundShape = ShapeBuilder(context)
        .withColor(Color.parseColor("#AAFFFFFF"))
        .withRadius(R.dimen.kyc_radius_default)
        .build()

    private val firstStateIcon = ContextCompat.getDrawable(
        context,
        R.drawable.kyc_ic_face_guide_state_1
    )

    private var timer: TimerTask? = null
    private var isShowOutOfFrame = false

    init {
        setTextColor(ThemeHolder.textColor)
        background = backgroundShape


        setCompoundDrawablesWithIntrinsicBounds(firstStateIcon, null, null, null)
        setTextColor(ContextCompat.getColor(context, R.color.kyc_color_red))

        setState(CaptureState.UNKNOWN)
    }

    fun setState(faceState: CaptureState) {
        if (faceState.isValid()) {
            stopTimer()
            return
        }

        if (isShowOutOfFrame) return

        if (faceState.isKeepStraight()) {
            startCountDown()
            setText(R.string.kyc_keep_straight)
            return
        }

        if (faceState.isMoveOut()) {
            startCountDown()
            setText(R.string.kyc_move_out)
            return
        }

        if (faceState.isUnknown()) {
            setText(R.string.kyc_face_state_1)
            return
        }


        if (faceState.isTooSmall()) {
            setText(R.string.kyc_face_state_3)
            setCompoundDrawablesWithIntrinsicBounds(firstStateIcon, null, null, null)
            setTextColor(ContextCompat.getColor(context, R.color.kyc_color_red))
            return
        }

        if (faceState.isTooBig()) {
            setText(R.string.kyc_face_state_4)
            setCompoundDrawablesWithIntrinsicBounds(firstStateIcon, null, null, null)
            setTextColor(ContextCompat.getColor(context, R.color.kyc_color_red))
            return
        }

        if (faceState.isLimited()) {
            setText(R.string.kyc_limited_time)
            return
        }

        if (faceState.isTooManyObject()) {
            setText(R.string.kyc_face_state_5)
            return
        }
    }

    private fun stopTimer() {
        isShowOutOfFrame = false
        timer?.cancel()
        timer = null
    }

    private fun startCountDown() {
        isShowOutOfFrame = true
        timer = Timer().schedule(3000) {
            isShowOutOfFrame = false
        }

    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopTimer()
    }
}
