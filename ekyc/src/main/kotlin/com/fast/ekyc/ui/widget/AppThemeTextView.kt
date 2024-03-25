package com.fast.ekyc.ui.widget

import android.content.Context
import android.util.AttributeSet
import com.fast.ekyc.theme.ThemeHolder

internal class AppThemeTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = android.R.attr.textViewStyle
) : AppRegularTextView(context, attrs, defStyleAttr) {
    init {
        setTextColor(ThemeHolder.textColor)
    }
}