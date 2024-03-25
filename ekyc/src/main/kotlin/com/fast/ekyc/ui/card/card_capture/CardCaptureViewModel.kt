package com.fast.ekyc.ui.card.card_capture

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.map
import com.fast.ekyc.R
import com.fast.ekyc.base.ui.BaseViewModel
import com.fast.ekyc.data.config.request.EkycConfig
import com.fast.ekyc.native.model.CardObject
import com.fast.ekyc.native.model.CardTypeAndSide
import com.fast.ekyc.ui.main.MainViewModel
import com.fast.ekyc.ui.state.CaptureState
import com.fast.ekyc.utils.extension.buildDisplayCardType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import javax.inject.Inject

internal class CardCaptureViewModel @Inject constructor(
    private val config: EkycConfig,
    private val context: Context,
) : BaseViewModel() {

    private var mainViewModel: MainViewModel? = null

    private var aiCardType = CardObject.AICardType.CMND

    private val _displayAcceptedCardTypes = MutableLiveData<String>()
    val displayAcceptedCardTypes: LiveData<String> = _displayAcceptedCardTypes

    private val _displayCardSide = MutableLiveData<String>()
    val displayCardSide: LiveData<String> = _displayCardSide

    private val _internalCardState = MutableStateFlow(CaptureState.UNKNOWN)
    val cardState: LiveData<CaptureState> = _internalCardState.debounce(50).asLiveData()


    fun isValidState(): LiveData<Boolean> = cardState.map { it.isValid() }

    private val _showFlash = MutableLiveData<Boolean>()
    val showFlash: LiveData<Boolean> = _showFlash

    val autoCapture = MutableLiveData<Boolean>()

    private val _showAutoCapture = MutableLiveData<Boolean>()
    val showAutoCapture: LiveData<Boolean> = _showAutoCapture

    private val _showCaptureButton = MutableLiveData<Boolean>()
    val showCaptureButton: LiveData<Boolean> = _showCaptureButton


    init {
        _displayAcceptedCardTypes.value = config.buildDisplayCardType(context)
        _showFlash.value = config.flash

        autoCapture.value = config.autoCaptureMode
        _showAutoCapture.value = config.showAutoCaptureButton
        _showCaptureButton.value = !(!config.showAutoCaptureButton && config.autoCaptureMode)
    }

    fun getAiCardType() = aiCardType

    fun setCardState(cardState: CaptureState) {
        _internalCardState.value = cardState
    }

    fun setMainViewModel(mainViewModel: MainViewModel) {
        this.mainViewModel = mainViewModel
        setDisplayCardSide()
    }

    private fun setDisplayCardSide(){
        mainViewModel?.let {
            if (config.idCardTypes.contains(EkycConfig.IdCardType.PASSPORT)) {
                _displayCardSide.value = ""
                return
            }

            if (isFrontSide()) {
                _displayCardSide.value = context.getString(R.string.kyc_front_card)
            } else {
                _displayCardSide.value = context.getString(R.string.kyc_back_card)
            }
        }
    }

    fun isFrontSide() = mainViewModel?.isFrontSide() ?: true

    fun isValidCardType(cardTypeAndSide: CardTypeAndSide): Boolean {
        aiCardType = cardTypeAndSide.aiCardType
        val cardType = cardTypeAndSide.cardType
        val requiredIdCardTypes = config.idCardTypes.toMutableList()

        if (cardType == EkycConfig.IdCardType.CCCD && !cardTypeAndSide.isFront && !cardTypeAndSide.isCCCDBackChip()) {
            if (!requiredIdCardTypes.contains(EkycConfig.IdCardType.CCCD)) {
                requiredIdCardTypes.add(EkycConfig.IdCardType.CCCD)
            }
        }

        if (!requiredIdCardTypes.contains(cardType)) return false
        if (cardType == EkycConfig.IdCardType.PASSPORT) return true

        return cardTypeAndSide.isFront == isFrontSide()
    }

}