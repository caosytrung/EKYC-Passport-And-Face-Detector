package com.fast.ekyc.ui.widget

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.util.AttributeSet
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import com.fast.ekyc.R
import com.fast.ekyc.theme.ThemeHolder
import com.fast.ekyc.ui.state.CaptureState
import com.fast.ekyc.utils.ShapeBuilder

internal class CardStateTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = android.R.attr.textViewStyle
) : AppRegularTextView(context, attrs, defStyleAttr) {
    private val backgroundShape = ShapeBuilder(context)
        .withColor(Color.WHITE)
        .withRadius(R.dimen.kyc_radius_default)
        .build()

    var cardSide: String? = null

    private val firstStateIcon = ContextCompat.getDrawable(
        context,
        R.drawable.kyc_ic_face_guide_state_1
    )

    init {
        setTextColor(ThemeHolder.textColor)
        background = backgroundShape
        setTextColor(ContextCompat.getColor(context, R.color.kyc_color_red))
        setState(CaptureState.VALID)
    }

    fun setState(captureState: CaptureState) {
        isGone = captureState.isValid()

        if (captureState.isLimited()) {
            setCompoundDrawablesWithIntrinsicBounds(firstStateIcon, null, null, null)
            setText(R.string.kyc_limited_time)
            return
        }

        if (captureState.isNotice()) {
            setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
            setText(R.string.kyc_card_preview_notice)
            return
        }

        if (captureState.isTooSmall()) {
            setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
            setText(R.string.kyc_card_state_2)
            return
        }

        if (captureState.isWrongType()) {
            setCompoundDrawablesWithIntrinsicBounds(firstStateIcon, null, null, null)

            setText(R.string.kyc_card_state_3)
            return
        }

        if (!captureState.isValid()) {
            cardSide?.let {
                val displayCard = if (it.isEmpty()) "" else "$it "
                val str =
                    SpannableStringBuilder("Để ${displayCard.lowercase()} trong khung")
                str.setSpan(
                    StyleSpan(Typeface.BOLD),
                    3,
                    3 + it.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                text = str
            }
        }
    }
}
