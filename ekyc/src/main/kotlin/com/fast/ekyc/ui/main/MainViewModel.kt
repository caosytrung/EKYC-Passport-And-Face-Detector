package com.fast.ekyc.ui.main

import android.graphics.Bitmap
import com.fast.ekyc.base.ui.BaseViewModel
import com.fast.ekyc.data.config.request.EkycConfig
import com.fast.ekyc.native.model.CardObject
import com.fast.ekyc.ui.face.face_capture.AdvancedFaceState
import com.fast.ekyc.utils.extension.isAdvancedMode
import com.fast.ekyc.utils.extension.isFront
import javax.inject.Inject

internal class MainViewModel @Inject constructor(private val config: EkycConfig) : BaseViewModel() {

    var uiFlowImageId: String = ""
        private set

    private var isFrontCardDone = false
    private var frontCardId: String = ""

    var firstAdvancedFace: FaceBitmapWithState? = null
    var secondAdvancedFace: FaceBitmapWithState? = null

    private var cardRetakeTime = 0
    private var faceRetakeTime = 0

    internal var cardType = CardObject.AICardType.CMND

    var videoPath: String? = null

    fun setAdvanceFace(index: Int, bitmap: Bitmap, state: AdvancedFaceState) {
        if (index == 0) {
            firstAdvancedFace = FaceBitmapWithState(bitmap, state)
        } else if (index == 1) {
            secondAdvancedFace = FaceBitmapWithState(bitmap, state)
        }
    }

    fun resetCardRetake() {
        cardRetakeTime = 0
    }

    fun resetFaceRetake() {
        faceRetakeTime = 0
    }

    fun decreaseCardRetake() {
        cardRetakeTime += 1
    }

    fun decreaseFaceRetake() {
        faceRetakeTime += 1
    }

    fun setFrontCardId(frontCardId: String) {
        this.frontCardId = frontCardId
    }

    fun setUiFlowImageId(uiFlowImageId: String) {
        this.uiFlowImageId = uiFlowImageId
    }

    fun markFistCardDone() {
        this.isFrontCardDone = true
    }

    fun isCardRetakeLimited() = cardRetakeTime > config.cardRetakeLimit

    fun isFaceRetakeLimited(): Boolean {
        return if (config.isAdvancedMode()) {
            faceRetakeTime > config.advancedLivenessConfig.challengeRetakeLimit
        } else {
            faceRetakeTime > config.faceRetakeLimit
        }
    }

    fun isFrontSide(): Boolean {
        return config.uiFlowType.isFront()
    }

    fun getTrackingCardSide() = if (isFrontSide()) "front" else "back"
}

internal class FaceBitmapWithState(val image: Bitmap, val state: AdvancedFaceState)