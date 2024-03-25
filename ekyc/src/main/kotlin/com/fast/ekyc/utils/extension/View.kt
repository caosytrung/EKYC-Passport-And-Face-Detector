package com.fast.ekyc.utils.extension

import android.graphics.BlendMode.SRC_ATOP
import android.graphics.BlendModeColorFilter
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.View
import android.view.View.OnClickListener
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout.LayoutParams
import androidx.annotation.DimenRes
import androidx.core.content.ContextCompat.getSystemService
import com.fast.ekyc.utils.listener.OnSingleClickListener

internal fun View.showKeyboard() {
    if (requestFocus()) {
        getSystemService(context, InputMethodManager::class.java)
            ?.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
    }
}

internal fun View.hideKeyboard(forceHide: Boolean = false) {
    val flags = if (forceHide) 0 else InputMethodManager.HIDE_IMPLICIT_ONLY
    getSystemService(context, InputMethodManager::class.java)
        ?.hideSoftInputFromWindow(windowToken, flags)
}

internal fun View.setBackgroundDrawableAndColor(
    drawable: Drawable? = null,
    color: Int? = null
) {
    drawable?.let {
        background = drawable
    }
    color?.let {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            background.colorFilter = BlendModeColorFilter(color, SRC_ATOP)
        } else {
            @Suppress("DEPRECATION")
            background.setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
        }
    }
}

internal fun View.setMargin(left: Int = 0, top: Int = 0, right: Int = 0, bottom: Int = 0) {
    val layoutParams = LayoutParams(layoutParams)
    layoutParams.setMargins(
        left,
        top,
        right,
        bottom
    )
    this.layoutParams = layoutParams
}

internal fun View.setOnSingleClickListener(
    clickInterval: Int = OnSingleClickListener.DEFAULT_CLICK_INTERVAL,
    listener: ((View) -> Unit)?
) {
    listener?.also {
        setOnClickListener(OnSingleClickListener(OnClickListener { it(this) }, clickInterval))
    } ?: setOnClickListener(null)
}

internal fun View.getDimen(@DimenRes dimen: Int): Int {
    return context.resources.getDimensionPixelSize(dimen)
}