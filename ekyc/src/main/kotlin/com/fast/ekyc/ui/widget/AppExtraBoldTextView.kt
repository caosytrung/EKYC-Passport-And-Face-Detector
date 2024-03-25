package com.fast.ekyc.ui.widget

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.res.ResourcesCompat
import com.fast.ekyc.R

internal class AppExtraBoldTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = android.R.attr.textViewStyle
) : AppCompatTextView(context, attrs, defStyleAttr) {
    init {
        typeface = ResourcesCompat.getFont(context, R.font.sarabun_extra_bold)
    }
}