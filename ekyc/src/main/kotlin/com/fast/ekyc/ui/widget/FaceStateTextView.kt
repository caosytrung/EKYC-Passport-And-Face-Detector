package com.fast.ekyc.ui.widget

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import androidx.core.content.ContextCompat
import com.fast.ekyc.R
import com.fast.ekyc.theme.ThemeHolder
import com.fast.ekyc.ui.state.CaptureState
import com.fast.ekyc.utils.ShapeBuilder

internal class FaceStateTextView @JvmOverloads constructor(
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

    private val secondStateIcon = ContextCompat.getDrawable(
        context,
        R.drawable.kyc_ic_done_18
    )


    init {
        setTextColor(ThemeHolder.textColor)
        background = backgroundShape

        setState(CaptureState.UNKNOWN)
    }

    fun setState(captureState: CaptureState) {
        if (captureState.isLimited()) {
            setText(R.string.kyc_limited_time)
            setCompoundDrawablesWithIntrinsicBounds(firstStateIcon, null, null, null)
            setTextColor(ContextCompat.getColor(context, R.color.kyc_color_red))
            return
        }

        if (captureState.isTooBig()) {
            setText(R.string.kyc_face_state_4)
            setCompoundDrawablesWithIntrinsicBounds(firstStateIcon, null, null, null)
            setTextColor(ContextCompat.getColor(context, R.color.kyc_color_red))
            return
        }

        if (captureState.isNotPortrait()) {
            setText(R.string.kyc_face_state_6)
            setCompoundDrawablesWithIntrinsicBounds(firstStateIcon, null, null, null)
            setTextColor(ContextCompat.getColor(context, R.color.kyc_color_red))
            return
        }

        if (captureState.isTooSmall()) {
            setText(R.string.kyc_face_state_3)
            setCompoundDrawablesWithIntrinsicBounds(firstStateIcon, null, null, null)
            setTextColor(ContextCompat.getColor(context, R.color.kyc_color_red))
            return
        }

        if (captureState.isTooManyObject()) {
            setText(R.string.kyc_face_state_5)
            setCompoundDrawablesWithIntrinsicBounds(firstStateIcon, null, null, null)
            setTextColor(ContextCompat.getColor(context, R.color.kyc_color_red))
            return
        }

        if (captureState.isUnknown()) {
            setText(R.string.kyc_face_state_1)
            setCompoundDrawablesWithIntrinsicBounds(firstStateIcon, null, null, null)
            setTextColor(ContextCompat.getColor(context, R.color.kyc_color_red))
            return
        }

        setText(R.string.kyc_face_state_2)
        setCompoundDrawablesWithIntrinsicBounds(secondStateIcon, null, null, null)
        setTextColor(ContextCompat.getColor(context, R.color.kyc_color_green))
    }

}
