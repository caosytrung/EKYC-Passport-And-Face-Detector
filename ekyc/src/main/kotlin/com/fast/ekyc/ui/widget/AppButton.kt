package com.fast.ekyc.ui.widget

import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.StateListDrawable
import android.os.Build
import android.util.AttributeSet
import android.util.StateSet
import android.util.TypedValue
import android.view.Gravity
import androidx.annotation.DimenRes
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.google.android.material.button.MaterialButton
import com.fast.ekyc.R
import com.fast.ekyc.theme.ThemeHolder
import com.fast.ekyc.utils.ShapeBuilder
import com.fast.ekyc.utils.extension.getDimen

class AppButton
@JvmOverloads
constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.materialButtonOutlinedStyle
) : MaterialButton(context, attrs, defStyleAttr) {

    private var buttonBackgroundColor = Color.WHITE
    private var buttonTextColor = ContextCompat.getColor(context, R.color.kyc_blue)
    private var buttonHeight = getDimen(R.dimen.kyc_button_height)

    init {
        configPrimaryColor()
        isAllCaps = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            backgroundTintList = null
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            letterSpacing = 0.0f
        }
        setTextSize(
            TypedValue.COMPLEX_UNIT_PX,
            context.resources.getDimension(R.dimen.kyc_text_size_title_larger)
        )
        setPadding(
            getDimen(R.dimen.kyc_margin_padding_default),
            0,
            getDimen(R.dimen.kyc_margin_padding_default),
            0
        )

        gravity = Gravity.CENTER

        try {
            typeface = ResourcesCompat.getFont(context, ThemeHolder.fontMedium)
        } catch (e: Resources.NotFoundException) {
            setDefaultFont()
        }
    }

    private fun setDefaultFont() {
        try {
            typeface = ResourcesCompat.getFont(context, R.font.sarabun_semibold)
        } catch (e: Resources.NotFoundException) {
            setDefaultFont()
        }
    }

    fun setButtonHeight(@DimenRes height: Int) {
        buttonHeight = getDimen(height)
    }

    fun setButtonBackgroundColor(color: Int) {
        buttonBackgroundColor = color
        configPrimaryColor()
    }

    fun setButtonTextColor(color: Int) {
        buttonTextColor = color
        configPrimaryColor()
    }

    private fun configPrimaryColor() {
        try {
            val radius = if (ThemeHolder.buttonCornerRadius == 0) {
                R.dimen.kyc_radius_extra_large
            } else {
                ThemeHolder.buttonCornerRadius
            }

            val normalStateShape = ShapeBuilder(context)
                .withColor(buttonBackgroundColor)
                .withRadius(radius)
                .build()

            val pressedStateShape = ShapeBuilder(context)
                .withColor(ContextCompat.getColor(context, R.color.kyc_face_action_unfocus))
                .withRadius(radius)
                .build()


            val backgroundStates =
                StateListDrawable()
            backgroundStates.addState(
                intArrayOf(android.R.attr.state_pressed), pressedStateShape
            )

            backgroundStates.addState(
                StateSet.WILD_CARD, normalStateShape
            )

            background = backgroundStates
            setTextColor(buttonTextColor)
        } catch (e: Exception) {
            val normalStateShape = ShapeBuilder(context)
                .withColor(buttonBackgroundColor)
                .withRadius(R.dimen.kyc_radius_extra_large)
                .build()

            val pressedStateShape = ShapeBuilder(context)
                .withColor(ContextCompat.getColor(context, R.color.kyc_face_action_unfocus))
                .withRadius(R.dimen.kyc_radius_extra_large)
                .build()


            val backgroundStates =
                StateListDrawable()
            backgroundStates.addState(
                intArrayOf(android.R.attr.state_pressed), pressedStateShape
            )

            backgroundStates.addState(
                StateSet.WILD_CARD, normalStateShape
            )

            background = backgroundStates
            setTextColor(buttonTextColor)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(
            widthMeasureSpec,
            MeasureSpec.makeMeasureSpec(buttonHeight, MeasureSpec.EXACTLY)
        )
    }
}
