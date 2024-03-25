package com.fast.ekyc.ui.widget.binding

import androidx.databinding.BindingAdapter
import com.fast.ekyc.ui.state.CaptureState
import com.fast.ekyc.ui.widget.overlay.CardCameraOverlayView
import com.fast.ekyc.ui.widget.overlay.FaceCameraOverlayView

internal object OverlayViewAdapters {

    @JvmStatic
    @BindingAdapter("frameState")
    fun setOverlay(
        view: CardCameraOverlayView,
        cardState: CaptureState?
    ) {
//        cardState?.let { view.setFramePaintColor(cardState) }
    }

    @JvmStatic
    @BindingAdapter("faceState")
    fun setFaceOverlayState(
        view: FaceCameraOverlayView,
        faceState: CaptureState?,
    ) {
        faceState?.let { view.setNormalFaceStateColor(faceState) }
    }

    @JvmStatic
    @BindingAdapter("isRecording", "faceAdvancedPercent", requireAll = true)
    fun setAdvancedFaceOverlayState(
        view: FaceCameraOverlayView,
        isRecording: Boolean,
        faceAdvancedPercent: Int,
    ) {
        view.setAdvancedFaceState(isRecording, faceAdvancedPercent)
    }

}