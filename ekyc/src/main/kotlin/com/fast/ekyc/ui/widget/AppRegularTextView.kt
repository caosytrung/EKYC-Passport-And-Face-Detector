package com.fast.ekyc.ui.widget

import android.content.Context
import android.content.res.Resources
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.res.ResourcesCompat
import com.fast.ekyc.theme.ThemeHolder

internal open class AppRegularTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = android.R.attr.textViewStyle
) : AppCompatTextView(context, attrs, defStyleAttr) {
    init {

        try {
            typeface = ResourcesCompat.getFont(context, ThemeHolder.fontRegular)
        } catch (e: Resources.NotFoundException) {

        }
    }
}