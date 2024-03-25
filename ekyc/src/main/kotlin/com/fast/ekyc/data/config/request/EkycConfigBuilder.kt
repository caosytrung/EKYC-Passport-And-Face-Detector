package com.fast.ekyc.data.config.request

import android.graphics.Color
import androidx.annotation.DimenRes
import androidx.annotation.FontRes
import com.fast.ekyc.R
import com.fast.ekyc.exception.InvalidConfigException
import com.fast.ekyc.theme.ThemeHolder
import com.fast.ekyc.utils.DataHolder
import com.fast.ekyc.utils.FaceIOUCaptureUtils
import com.fast.ekyc.utils.extension.isFrontCardOnly

class EkycConfigBuilder{

    private var isCacheImage: Boolean = false
    private var idCardTypes: List<EkycConfig.IdCardType> = listOf()
    private var uiFlowType: EkycConfig.UiFlowType = EkycConfig.UiFlowType.ID_CARD_FRONT
    private var idCardAbbr: Boolean = true

    private var faceAdvancedMode = EkycConfig.FaceAdvancedMode.RESTRICTED
    private var advancedLivenessConfig = AdvancedLivenessConfig()


    private var showHelp: Boolean = true
    private var autoCaptureMode: Boolean = true
    private var showAutoCaptureButton: Boolean = true
    private var backgroundColor: Int = Color.parseColor("#66000000")
    private var textColor: Int = Color.WHITE
    private var popupBackgroundColor: Int = Color.parseColor("#EFEFEE")
    private var buttonColor: Int = Color.parseColor("#EE0033")
    private var buttonCornerRadius: Int = R.dimen.kyc_radius_extra_large
    private var fontRegular = R.font.sarabun_re
    private var fontMedium = R.font.sarabun_semibold

    private var flash: Boolean = true
    private var zoom: Int = 1

    private var selfieCameraMode: EkycConfig.CameraMode = EkycConfig.CameraMode.FRONT
    private var idCardCameraMode: EkycConfig.CameraMode = EkycConfig.CameraMode.BACK

    private var skipConfirmScreen: Boolean = false

    private var faceMinRatio: Float = 0.25f
    private var faceMaxRatio: Float = 0.7f
    private var idCardMinRatio: Float = 0.6f
    private var faceRetakeLimit: Int = 10
    private var cardRetakeLimit: Int = 10

    private var isDebug: Boolean = false
    private var iouThreshold: Float = 0.92f
    private var iouCaptureTime: Int = 1000

    private var faceBottomPercentage: Float = 0.15f
    private var idCardBoxPercentage: Float = 0.025f

    fun setIdCardTypes(idType: List<EkycConfig.IdCardType>) = apply {
        this.idCardTypes = idType
    }

    fun setUiFlowType(uiFlowType: EkycConfig.UiFlowType) = apply {
        this.uiFlowType = uiFlowType
    }

    fun setIdCardAbbr(idCardAbbr: Boolean) = apply {
        this.idCardAbbr = idCardAbbr
    }

    fun isCacheImage(isCacheImage: Boolean) = apply {
        this.isCacheImage = isCacheImage
    }

    fun setFaceAdvancedMode(faceAdvancedMode: EkycConfig.FaceAdvancedMode) = apply {
        this.faceAdvancedMode = faceAdvancedMode
    }

    fun setAdvancedLivenessConfig(advancedLivenessConfig: AdvancedLivenessConfig) = apply {
        this.advancedLivenessConfig = advancedLivenessConfig
    }

    fun setShowHelp(showHelp: Boolean) = apply {
        this.showHelp = showHelp
    }

    fun setShowAutoCaptureButton(showAutoCaptureButton: Boolean) = apply {
        this.showAutoCaptureButton = showAutoCaptureButton
    }

    fun setAutoCaptureMode(isOn: Boolean) = apply {
        this.autoCaptureMode = isOn
    }

    fun setBackgroundColor(color: Int) = apply {
        this.backgroundColor = color
    }

    fun setTextColor(color: Int) = apply {
        this.textColor = color
    }

    fun setPopupBackgroundColor(color: Int) = apply {
        this.popupBackgroundColor = color
    }

    fun setButtonColor(color: Int) = apply {
        this.buttonColor = color
    }

    fun setButtonCornerRadius(@DimenRes buttonCornerRadius: Int) = apply {
        this.buttonCornerRadius = buttonCornerRadius
    }

    fun setFonts(@FontRes regularFont: Int, @FontRes mediumFont: Int) = apply {
        this.fontRegular = regularFont
        this.fontMedium = mediumFont
    }

    fun setShowFlashButton(flash: Boolean) = apply {
        this.flash = flash
    }

    fun setZoom(zoom: Int) = apply {
        this.zoom = zoom
    }

    fun setSelfieCameraMode(selfieCameraMode: EkycConfig.CameraMode) = apply {
        this.selfieCameraMode = selfieCameraMode
    }

    fun setIdCardCameraMode(idcardCameraMode: EkycConfig.CameraMode) = apply {
        this.idCardCameraMode = idcardCameraMode
    }

    fun setDebug(isDebug: Boolean) = apply {
        this.isDebug = isDebug
    }

    fun setIouThreshold(iouThreshold: Float) = apply {
        this.iouThreshold = iouThreshold
    }

    fun setIouCaptureTime(iouCaptureTime: Int) = apply {
        this.iouCaptureTime = iouCaptureTime
    }

    fun setSkipConfirmScreen(showConfirmScreen: Boolean) = apply {
        this.skipConfirmScreen = showConfirmScreen
    }

    fun setFaceMinRatio(ratio: Float) = apply {
        faceMinRatio = ratio
    }

    fun setFaceMaxRatio(ratio: Float) = apply {
        faceMaxRatio = ratio
    }

    fun setIdCardMinRatio(ratio: Float) = apply {
        idCardMinRatio = ratio
    }

    fun setFaceRetakeLimit(faceRetakeLimit: Int) = apply {
        this.faceRetakeLimit = faceRetakeLimit
    }

    fun setIdCardRetakeLimit(cardRetakeLimit: Int) = apply {
        this.cardRetakeLimit = cardRetakeLimit
    }

    fun setFaceBottomPercentage(faceBottomPercentage: Float) = apply {
        this.faceBottomPercentage = faceBottomPercentage
    }

    fun setIdCardBoxPercentage(idCardBoxPercentage: Float) = apply {
        this.idCardBoxPercentage = idCardBoxPercentage
    }

    @Throws
    private fun validationInput() {
        if (uiFlowType.isIdCardBack() && idCardTypes.isFrontCardOnly()) {
            throw InvalidConfigException.WrongFlowType
        }
    }

    @Throws
    fun build(): EkycConfig {
        validationInput()

        // user Type All as the default behavior
        if (idCardTypes.isEmpty()) idCardTypes = idCardTypes.toMutableList().apply {
            addAll(mutableSetOf(EkycConfig.IdCardType.CMND, EkycConfig.IdCardType.CCCD))
        }

        ThemeHolder.buttonColor = buttonColor
        ThemeHolder.backgroundColor = backgroundColor
        ThemeHolder.textColor = textColor
        ThemeHolder.popupBackgroundColor = popupBackgroundColor
        ThemeHolder.buttonCornerRadius = buttonCornerRadius
        ThemeHolder.fontRegular = fontRegular
        ThemeHolder.fontMedium = fontMedium

        FaceIOUCaptureUtils.THRESHOLD = iouThreshold
        FaceIOUCaptureUtils.MAX_TIME_IN_MILLI_SECOND = iouCaptureTime
        DataHolder.clear()

        return EkycConfig(
            idCardTypes = idCardTypes,
            uiFlowType = uiFlowType,
            isCacheImage = isCacheImage,
            faceAdvancedMode = faceAdvancedMode,
            idCardAbbr = idCardAbbr,
            showHelp = showHelp,
            showAutoCaptureButton = showAutoCaptureButton,
            autoCaptureMode = autoCaptureMode,
            backgroundColor = backgroundColor,
            textColor = textColor,
            popupBackgroundColor = popupBackgroundColor,
            buttonColor = buttonColor,
            flash = flash,
            zoom = zoom,
            selfieCameraMode = selfieCameraMode,
            idCardCameraMode = idCardCameraMode,
            isDebug = isDebug,
            skipConfirmScreen = skipConfirmScreen,
            faceMinRatio = faceMinRatio,
            faceMaxRatio = faceMaxRatio,
            idCardMinRatio = idCardMinRatio,
            faceRetakeLimit = faceRetakeLimit,
            cardRetakeLimit = cardRetakeLimit,
            faceBottomPercentage = faceBottomPercentage,
            idCardBoxPercentage = idCardBoxPercentage,
            advancedLivenessConfig
        )
    }
}