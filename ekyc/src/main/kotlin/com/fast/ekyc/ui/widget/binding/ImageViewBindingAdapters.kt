package com.fast.ekyc.ui.widget.binding

import android.widget.RelativeLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.databinding.BindingAdapter
import com.fast.ekyc.R
import com.fast.ekyc.utils.extension.getDimen

internal object ImageViewBindingAdapters {


    @JvmStatic
    @BindingAdapter("srcCompat", "isFocused", "isNotFaceDone", requireAll = true)
    fun setSelection(
        view: AppCompatImageView,
        srcCompat: Int,
        isFocused: Boolean,
        isNotFaceDone: Boolean,
    ) {

        val params = view.layoutParams as RelativeLayout.LayoutParams

        val size = if (isFocused) {
            view.getDimen(R.dimen.kyc_advanced_guide_image_focus)
        } else {
            view.getDimen(R.dimen.kyc_advanced_guide_image_unfocus)
        }

        params.width = size
        params.height = size
        view.layoutParams = params

        val drawableRes = R.drawable.kyc_bg_advanced_focused
//            if (isFocused) R.drawable.kyc_bg_advanced_focused
//            else R.drawable.kyc_bg_advanced_unfocused
        view.setBackgroundResource(drawableRes)

        if (isNotFaceDone) {
            view.setImageResource(R.drawable.kyc_ic_more)
        } else {
            view.setImageResource(srcCompat)
        }
    }
}