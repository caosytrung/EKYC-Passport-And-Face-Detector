package com.fast.ekyc.ui.face.face_capture

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.fast.ekyc.R
import com.fast.ekyc.base.ui.BaseViewModel
import com.fast.ekyc.base.ui.SingleHandlerEvent
import com.fast.ekyc.data.config.request.AdvancedLivenessConfig
import com.fast.ekyc.data.config.request.EkycConfig
import com.fast.ekyc.ui.main.MainViewModel
import com.fast.ekyc.ui.state.CaptureState
import com.fast.ekyc.ui.widget.binding.CurrentFaceProgressState
import com.fast.ekyc.utils.CombinedLiveData
import com.fast.ekyc.utils.extension.getDisplayIcon
import com.fast.ekyc.utils.extension.getDisplayText
import com.fast.ekyc.utils.extension.isAdvancedMode
import com.fast.ekyc.utils.timer.CountDownTimer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

internal class FaceCaptureViewModel @Inject constructor(
    private val config: EkycConfig,
    // applicationContext
    private val context: Context,
) : BaseViewModel() {
    private var timer: CountDownTimer? = null
    private var actionList: List<AdvancedFaceState> =
        AdvancedLivenessConfig.getActionList().map { it.toInternalAction() }

    /**
     * This flag was used in advanced mode, when user want to capture image before start recording
     */
    private var isAdvancedCaptured = false

    private val _doneEvent = MutableLiveData<SingleHandlerEvent<Unit>>()
    val doneEvent: LiveData<SingleHandlerEvent<Unit>> = _doneEvent

    private val _retakeEvent = MutableLiveData<SingleHandlerEvent<Unit>>()
    val retakeEvent: LiveData<SingleHandlerEvent<Unit>> = _retakeEvent

    private val _limitTimeErrorEvent = MutableLiveData<SingleHandlerEvent<Unit>>()
    val limitTimeErrorEvent: LiveData<SingleHandlerEvent<Unit>> = _limitTimeErrorEvent

    private val _isAdvancedMode = MutableLiveData<Boolean>()
    val isAdvancedMode: LiveData<Boolean> = _isAdvancedMode

    private val _advancedFacePercent = MutableLiveData<Int>(0)
    val advancedFacePercent: LiveData<Int> = _advancedFacePercent
    val displayAdvancedFacePercent: LiveData<String> = _advancedFacePercent.map {
        "$it %"
    }

    private val _internalFaceState = MutableLiveData(CaptureState.UNKNOWN)
    val faceState: LiveData<CaptureState> = _internalFaceState

    val isShowAdvanced = CombinedLiveData(
        source1 = _isAdvancedMode,
        source2 = faceState,
        combine = { isAdvancedMode, faceState ->
            if (config.faceAdvancedMode.isRestricted()) {
                isAdvancedMode == true && faceState?.isCardInsideFrame() == true
            } else {
                isAdvancedMode == true && isAdvancedCaptured
            }

        }
    )

    private val _currentAdvancedIndex = MutableLiveData<SingleHandlerEvent<Int?>>()
    val currentAdvancedIndex: LiveData<SingleHandlerEvent<Int?>> = _currentAdvancedIndex

    val currentBindingIndex: LiveData<Int> = _currentAdvancedIndex.map { it?.peekContent() ?: 0 }

    val firstFaceAdvancedProgressState: LiveData<CurrentFaceProgressState> =
        currentBindingIndex.map {
            if (it == 0) CurrentFaceProgressState.READY
            else CurrentFaceProgressState.DONE
        }

    val secondFaceAdvancedProgressState: LiveData<CurrentFaceProgressState> =
        currentBindingIndex.map {
            if (it == 0) CurrentFaceProgressState.NOT_READY
            else CurrentFaceProgressState.READY
        }

    val currentAdvancedText: LiveData<String> = currentAdvancedIndex.map {
        context.getString(
            actionList[minOf(
                currentAdvancedIndex.value?.peekContent() ?: 0,
                actionList.size - 1
            )].getDisplayText()
        )
    }

    private val _firstAdvancedResourceId = MutableLiveData<Int>()
    val firstAdvancedResourceId: LiveData<Int> = _firstAdvancedResourceId

    private val _secondAdvancedResourceId = MutableLiveData<Int>()
    val secondAdvancedResourceId: LiveData<Int> = _secondAdvancedResourceId

    private val _remainingTime = MutableLiveData<String>()
    val remainingTime: LiveData<String> = _remainingTime

    private val _showFlash = MutableLiveData<Boolean>()
    val showFlash: LiveData<Boolean> = _showFlash

    private val _showHelp = MutableLiveData<Boolean>()
    val showHelp: LiveData<Boolean> = _showHelp

    val autoCapture = MutableLiveData<Boolean>()

    private val _showAutoCapture = MutableLiveData<Boolean>()
    val showAutoCapture: LiveData<Boolean> = _showAutoCapture

    private val _showCaptureButton = MutableLiveData<Boolean>()
    val showCaptureButton: LiveData<Boolean> = _showCaptureButton

    private var mainViewModel: MainViewModel? = null

    init {
        _isAdvancedMode.value = config.isAdvancedMode()
        autoCapture.value = config.autoCaptureMode
        _showAutoCapture.value = config.showAutoCaptureButton
        _showCaptureButton.value = !(!config.showAutoCaptureButton && config.autoCaptureMode)
        _showFlash.value = config.flash
        _showHelp.value = !config.isAdvancedMode()

        updateActionList()
    }

    fun setMainViewModel(mainViewModel: MainViewModel) {
        this.mainViewModel = mainViewModel
    }

    private fun updateActionList() {
        _firstAdvancedResourceId.value = actionList[0].getDisplayIcon()
        _secondAdvancedResourceId.value = actionList[1].getDisplayIcon()
    }

    fun setAdvancedCaptured(value: Boolean) {
        isAdvancedCaptured = value
    }

    fun isStraightAdvancedCaptured() = isAdvancedCaptured

    fun resetState() {
        _internalFaceState.postValue(CaptureState.UNKNOWN)

        if (config.isAdvancedMode()) {
            setAdvancedCaptured(false)
            _currentAdvancedIndex.value = SingleHandlerEvent(null)
            stopTimer()
        }
    }


    fun setFaceState(cardState: CaptureState) {
        if (config.isAdvancedMode()) {
            handleAdvancedModeState(cardState)
        } else {
            _internalFaceState.postValue(cardState)
        }
    }

    private fun handleAdvancedModeState(cardState: CaptureState) {
        if (cardState.isCardInsideFrame()) {

            // case from outside to inside
            if (_internalFaceState.value?.isCardInsideFrame() == false) {
                if (!isAdvancedCaptured) {
                    onStartRecording(cardState)
                    return
                }
            }
        } else {

            // case from inside to outside
            if (_internalFaceState.value?.isCardInsideFrame() == true) {

                if (!cardState.isValid()) {
                    val description = when (cardState) {
                        CaptureState.MOVE_OUT_OF_FRAME -> context.getString(R.string.kyc_move_out)
                        CaptureState.UNKNOWN -> context.getString(R.string.kyc_move_out)
                        CaptureState.TOO_MANY_OBJECT -> context.getString(R.string.kyc_face_state_5)
                        else -> null
                    }
                    description?.let {
                        val currentAction = getCurrentAction()
                        val photoInfo =
                            if (currentAction == AdvancedFaceState.LEFT) "1#null_2#nul" else "1#left_2#nul"
                    }
                }

                if (config.faceAdvancedMode.isRestricted()) {
                    setAdvancedCaptured(false)
                    stopTimer()

                }
            }
        }

        _internalFaceState.postValue(cardState)
    }

    private fun onStartRecording(cardState: CaptureState) {
        viewModelScope.launch(Dispatchers.Main) {
            // should delay here to make play 'start recording voice before first action's voice'
            delay(1500)
            _internalFaceState.postValue(cardState)
            startTimer()
        }
    }

    fun getCurrentAction(): AdvancedFaceState {
        val index = currentAdvancedIndex.value?.peekContent() ?: 0
        if (index >= actionList.size) return AdvancedFaceState.PORTRAIT

        return actionList[index]
    }

    private fun stopTimer() {
        timer?.cancel()
    }

    fun startTimer() {
        _currentAdvancedIndex.value = SingleHandlerEvent(0)

        timer?.cancel()
        timer = object : CountDownTimer(
            config.advancedLivenessConfig.duration * 1000L,
            TimeUnit.SECONDS.toMillis(1)
        ) {
            override fun onFinish() {
                handleTimerFinish()
            }

            override fun onTick(remainingMillis: Long) {
                val time = TimeUnit.MILLISECONDS.toSeconds(remainingMillis)

                _remainingTime.postValue("0:${time} s")
            }
        }

        timer?.start(viewModelScope)
    }

    private fun handleTimerFinish() {
        timer?.cancel()

        mainViewModel?.apply {
            decreaseFaceRetake()
            if (isFaceRetakeLimited()) {
                _retakeEvent.value = SingleHandlerEvent(Unit)
            } else {
                _limitTimeErrorEvent.postValue(SingleHandlerEvent(Unit))
            }
        }
    }

    fun nextAction() {
        val currentIndex = getCurrentIndex()
        _currentAdvancedIndex.value = SingleHandlerEvent(currentIndex + 1)

        if (currentIndex >= 1) {
            _doneEvent.postValue(SingleHandlerEvent(Unit))
            stopTimer()
        }
    }

    fun getCurrentIndex() = currentAdvancedIndex.value?.peekContent() ?: 0

    fun setAdvancedPercent(percent: Int) {
        _advancedFacePercent.postValue(percent)
    }
}