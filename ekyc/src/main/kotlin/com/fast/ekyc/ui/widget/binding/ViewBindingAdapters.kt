package com.fast.ekyc.ui.widget.binding

import android.graphics.Color
import android.view.View
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.databinding.BindingAdapter
import com.fast.ekyc.R
import com.fast.ekyc.utils.ShapeBuilder

internal object ViewBindingAdapters {

    @JvmStatic
    @BindingAdapter("visible")
    fun setVisible(
        view: View,
        isVisible: Boolean
    ) {
        view.isVisible = isVisible
    }

    @JvmStatic
    @BindingAdapter("invisible")
    fun setInvisible(
        view: View,
        invisible: Boolean
    ) {
        view.isInvisible = invisible
    }

    @JvmStatic
    @BindingAdapter("android:enabled")
    fun setEnable(
        view: View,
        enabled: Boolean?
    ) {
        enabled?.let { view.isEnabled = enabled }
    }


    @JvmStatic
    @BindingAdapter("faceProgressState")
    fun setFaceProgressView(
        view: View,
        faceProgressState: CurrentFaceProgressState?,
    ) {
        faceProgressState?.let {
            val color = when (it) {
                CurrentFaceProgressState.NOT_READY -> Color.parseColor("#80FFFFFF")
                CurrentFaceProgressState.READY -> Color.WHITE
                CurrentFaceProgressState.DONE -> Color.GREEN

            }

            val shape = ShapeBuilder(view.context)
                .withColor(color)
                .withRadius(R.dimen.kyc_radius_default_smaller)
                .build()

            view.background = shape
        }

    }
}

internal enum class CurrentFaceProgressState {
    NOT_READY, READY, DONE
}