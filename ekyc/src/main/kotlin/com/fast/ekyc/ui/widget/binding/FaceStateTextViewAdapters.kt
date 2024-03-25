package com.fast.ekyc.ui.widget.binding

import androidx.databinding.BindingAdapter
import com.fast.ekyc.ui.state.CaptureState
import com.fast.ekyc.ui.widget.CardStateTextView
import com.fast.ekyc.ui.widget.FaceAdvancedStateTextView
import com.fast.ekyc.ui.widget.FaceStateTextView

internal object FaceStateTextViewAdapters {
    @JvmStatic
    @BindingAdapter("faceGuideState")
    fun setGuideState(
        view: FaceStateTextView,
        cardState: CaptureState?
    ) {
        cardState?.let { view.setState(cardState) }
    }

    @JvmStatic
    @BindingAdapter("faceAdvancedGuideState")
    fun setGuideState(
        view: FaceAdvancedStateTextView,
        cardState: CaptureState?
    ) {
        cardState?.let { view.setState(cardState) }
    }

    @JvmStatic
    @BindingAdapter("cardGuideState")
    fun setGuideState(
        view: CardStateTextView,
        cardState: CaptureState?
    ) {
        cardState?.let { view.setState(cardState) }
    }
}