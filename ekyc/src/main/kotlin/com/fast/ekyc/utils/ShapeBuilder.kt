package com.fast.ekyc.utils

import android.content.Context
import android.graphics.drawable.GradientDrawable
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.core.content.ContextCompat

internal class ShapeBuilder(private val context: Context) {
    private val shape: GradientDrawable = GradientDrawable()

    fun withRadius(@DimenRes radius: Int) = this.apply {
        shape.cornerRadius = context.resources.getDimensionPixelSize(radius).toFloat()
    }

    fun withRadius(
        @DimenRes topLeft: Int? = null,
        @DimenRes topRight: Int? = null,
        @DimenRes bottomRight: Int? = null,
        @DimenRes bottomLeft: Int? = null
    ) = this.apply {

        val topLeftRadius =
            topLeft?.let { context.resources.getDimensionPixelSize(it).toFloat() } ?: 0f

        val topRightRadius =
            topRight?.let { context.resources.getDimensionPixelSize(it).toFloat() } ?: 0f

        val bottomRightRadius =
            bottomRight?.let { context.resources.getDimensionPixelSize(it).toFloat() } ?: 0f

        val bottomLeftRadius =
            bottomLeft?.let { context.resources.getDimensionPixelSize(it).toFloat() } ?: 0f

        shape.cornerRadii = floatArrayOf(
            topLeftRadius,
            topLeftRadius,
            topRightRadius,
            topRightRadius,
            bottomRightRadius,
            bottomRightRadius,
            bottomLeftRadius,
            bottomLeftRadius
        )
    }

    fun withColor(color: Int) = this.apply {
        shape.setColor(color)
    }

    fun withStroke(color: Int, @DimenRes borderWidth: Int) = this.apply {
        shape.setStroke(context.resources.getDimensionPixelSize(borderWidth), color)
    }

    fun withStrokeColorRes(@ColorRes color: Int, @DimenRes borderWidth: Int) = this.apply {
        shape.setStroke(
            context.resources.getDimensionPixelSize(borderWidth),
            ContextCompat.getColor(context, color)
        )
    }

    fun build() = shape

}