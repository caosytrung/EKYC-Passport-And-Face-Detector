package com.fast.ekyc.ui.card.card_preview

import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.fast.ekyc.R
import com.fast.ekyc.base.ui.BaseViewModel
import com.fast.ekyc.base.ui.SingleHandlerEvent
import com.fast.ekyc.data.config.request.EkycConfig
import com.fast.ekyc.native.model.CardObject
import com.fast.ekyc.ui.main.MainViewModel
import com.fast.ekyc.utils.DataHolder
import com.fast.ekyc.utils.extension.buildDisplayCardType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

internal class CardPreviewViewModel @Inject constructor(
    private val config: EkycConfig,
    // application context to prevent the memory leak issues.
    private val context: Context,
) : BaseViewModel() {

    private var mainViewModel: MainViewModel? = null
    internal var cardType = CardObject.AICardType.CMND

    private val _uiFlowDone = MutableLiveData<SingleHandlerEvent<Unit>>()
    val uiFlowDone: LiveData<SingleHandlerEvent<Unit>> = _uiFlowDone

    private val _moveToFaceEvent = MutableLiveData<SingleHandlerEvent<Unit>>()
    val moveToFaceEvent: LiveData<SingleHandlerEvent<Unit>> = _moveToFaceEvent

    private val _moveToBackCardWithFrontCardIdEvent = MutableLiveData<SingleHandlerEvent<Unit>>()
    val moveToBackCardEvent: LiveData<SingleHandlerEvent<Unit>> =
        _moveToBackCardWithFrontCardIdEvent

    private val _displayAcceptedCardTypes = MutableLiveData<String>()
    val displayAcceptedCardTypes: LiveData<String> = _displayAcceptedCardTypes

    private val _showMessageErrorDialogEvent = MutableLiveData<SingleHandlerEvent<String>>()
    val showMessageErrorDialogEvent: LiveData<SingleHandlerEvent<String>> =
        _showMessageErrorDialogEvent

    private val _showNoticeErrorDialogEvent =
        MutableLiveData<SingleHandlerEvent<Pair<String, String>>>()
    val showNoticeErrorDialogEvent: LiveData<SingleHandlerEvent<Pair<String, String>>> =
        _showNoticeErrorDialogEvent

    private val _displayCardSide = MutableLiveData<String>()
    val displayCardSide: LiveData<String> = _displayCardSide

    private val _showHelp = MutableLiveData<Boolean>()
    val showHelp: LiveData<Boolean> = _showHelp

    init {
        _displayAcceptedCardTypes.value = config.buildDisplayCardType(context)
        _showHelp.value = config.showHelp
    }

    fun setCardType(cardType: CardObject.AICardType) {
        this.cardType = cardType
        mainViewModel?.cardType = cardType
    }

    fun setMainViewModel(mainViewModel: MainViewModel) {
        this.mainViewModel = mainViewModel
        setDisplayCardSide()
    }

    private fun setDisplayCardSide() {
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

    private fun isFrontSide() = mainViewModel?.isFrontSide() ?: true

    override fun onCleared() {
        mainViewModel = null
        super.onCleared()
    }

    fun verifyImage(bitmap: Bitmap) {
        viewModelScope.launch(Dispatchers.IO) {
            handleUIOnly(bitmap)
        }
    }

    private suspend fun handleUIOnly(bitmap: Bitmap) {
        showLoading()
        saveLocalImage(bitmap)
        hideLoading()

        withContext(Dispatchers.Main) {
            _uiFlowDone.value = SingleHandlerEvent(Unit)
        }
    }

    private fun saveLocalImage(croppedBitmap: Bitmap) {
        val cachedBitmap = DataHolder.getCardBitmap()
        if (config.isCacheImage) {
            DataHolder.uiFlowLocalFullImage = cachedBitmap
        }
        DataHolder.uiFlowLocalUploadImage = croppedBitmap
    }
}

enum class LabelPose(val value: String) {
    Left("Left"),
    Right("Right"),
    Top("Top"),
    Portrait("Portrait"),
}
