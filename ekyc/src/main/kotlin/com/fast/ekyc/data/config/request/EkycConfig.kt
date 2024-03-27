package com.fast.ekyc.data.config.request

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class EkycConfig internal constructor(
    val idCardTypes: List<IdCardType>,
    val uiFlowType: UiFlowType,
    val isCacheImage: Boolean,
    val faceAdvancedMode: FaceAdvancedMode,
    val idCardAbbr: Boolean,
    val showHelp: Boolean,
    val showAutoCaptureButton: Boolean,
    val autoCaptureMode: Boolean,
    val backgroundColor: Int,
    val textColor: Int,
    val popupBackgroundColor: Int,
    val buttonColor: Int,
    var flash: Boolean,
    val zoom: Int,
    val selfieCameraMode: CameraMode,
    val idCardCameraMode: CameraMode,
    val isDebug: Boolean,
    val faceMinRatio: Float,
    val faceMaxRatio: Float,
    val idCardMinRatio: Float,
    val faceRetakeLimit: Int,
    val cardRetakeLimit: Int,
    val faceBottomPercentage: Float,
    val idCardBoxPercentage: Float,
    val advancedLivenessConfig: AdvancedLivenessConfig,
) : Parcelable {

    enum class NfcVerifyOption(val type: String) {
        FACE_VERIFY("face_verify"), CHIP_DATA_VERIFY("chip_data_verify");
    }

    enum class CameraMode {
        FRONT, BACK;

        fun isBack() = this == BACK
    }

    enum class FaceAdvancedMode {
        RESTRICTED, UNRESTRICTED;

        fun isRestricted() = this == RESTRICTED
    }

    enum class IdCardType {
        CMND,
        CCCD,
        PASSPORT,
        BLX,
        CMQD;
    }

    enum class UiFlowType {
        ID_CARD_FRONT,
        ID_CARD_BACK,
        FACE_BASIC,
        FACE_ADVANCED;

        fun isIdCardBack() = this == ID_CARD_BACK
    }
}

@Parcelize
data class AdvancedLivenessConfig(
    val leftAngle: Int = 30,
    val rightAngle: Int = 30,
    val challengeRetakeLimit: Int = 4,
    val duration: Int = 20,
) : Parcelable {


    enum class Action(val value: String) {
        LEFT("Left"),
        RIGHT("Right"),
    }

    companion object {
        internal fun getActionList() = listOf(Action.LEFT, Action.RIGHT)
    }
}
