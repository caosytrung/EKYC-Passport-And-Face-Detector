package com.fast.ekyc.ui.widget

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.StateListDrawable
import android.util.AttributeSet
import android.util.StateSet
import androidx.core.content.ContextCompat
import com.google.android.material.switchmaterial.SwitchMaterial
import com.fast.ekyc.R
import com.fast.ekyc.theme.ThemeHolder
import com.fast.ekyc.utils.ShapeBuilder


internal class AppSwitch
@JvmOverloads
constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.switchStyle
) : SwitchMaterial(context, attrs, defStyleAttr) {

    private val switchColor = Color.WHITE
    private val switchOff = ContextCompat.getColor(context, R.color.kyc_switch_off)

    init {
        isUseMaterialThemeColors = false
        setTrackColor()
    }

    override fun setChecked(checked: Boolean) {
        super.setChecked(checked)
        jumpDrawablesToCurrentState()
    }

    private fun setTrackColor() {
        val switchOnDrawable = ShapeBuilder(context)
            .withColor(switchColor)
            .withRadius(R.dimen.kyc_radius_large)
            .build()
        val switchOffDrawable = ShapeBuilder(context)
            .withColor(switchOff)
            .withRadius(R.dimen.kyc_radius_large)
            .build()

        val states = StateListDrawable()
        states.addState(
            intArrayOf(android.R.attr.state_enabled, android.R.attr.state_checked),
            switchOnDrawable
        )
        states.addState(
            intArrayOf(android.R.attr.state_enabled, -android.R.attr.state_checked),
            switchOffDrawable
        )

        trackDrawable = states

        thumbTintList = colorStateListOf(
            intArrayOf(android.R.attr.state_checked) to ThemeHolder.buttonColor,
            StateSet.WILD_CARD to Color.WHITE
        )
    }

}


fun colorStateListOf(vararg mapping: Pair<IntArray, Int>): ColorStateList {
    val (states, colors) = mapping.unzip()
    return ColorStateList(states.toTypedArray(), colors.toIntArray())

}