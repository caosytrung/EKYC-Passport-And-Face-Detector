package com.fast.ekyc.ui.widget

import android.content.Context
import android.content.res.Resources
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.res.ResourcesCompat
import com.fast.ekyc.R
import com.fast.ekyc.theme.ThemeHolder

internal open class AppBoldTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = android.R.attr.textViewStyle
) : AppCompatTextView(context, attrs, defStyleAttr) {
    init {
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
}