package com.fast.ekyc.ui.face.face_capture

import com.fast.ekyc.data.config.request.AdvancedLivenessConfig
import com.fast.ekyc.ui.card.card_preview.LabelPose

internal enum class AdvancedFaceState {
    LEFT,
    RIGHT,
    PORTRAIT,
}

internal fun AdvancedLivenessConfig.Action.toInternalAction(): AdvancedFaceState {
    return when (this) {
        AdvancedLivenessConfig.Action.LEFT -> AdvancedFaceState.LEFT
        AdvancedLivenessConfig.Action.RIGHT -> AdvancedFaceState.RIGHT
    }
}

internal fun AdvancedFaceState.toLabelPose(): LabelPose {
    return when (this) {
        AdvancedFaceState.LEFT -> LabelPose.Right
        AdvancedFaceState.RIGHT -> LabelPose.Left
        AdvancedFaceState.PORTRAIT -> LabelPose.Portrait
    }
}