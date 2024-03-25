package com.fast.ekyc.utils.extension

import android.graphics.Rect
import android.graphics.RectF

internal fun Rect.area() = width() * height()
internal fun RectF.area() = width() * height()
internal fun Rect.scaleLarger(scale: Float): Rect {
    val delta = (width() * scale - width()).toInt() / 2

    return Rect(
        this.left - delta,
        this.top - delta,
        this.right + delta,
        this.bottom + delta,
    )
}

internal fun RectF.scaleLarger(scale: Float): RectF {
    val delta = (width() * scale - width()) / 2

    return RectF(
        this.left - delta,
        this.top - delta,
        this.right + delta,
        this.bottom + delta,
    )
}
