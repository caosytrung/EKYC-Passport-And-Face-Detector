package com.fast.ekyc.ui.face.face_preview

import android.graphics.Bitmap
import android.graphics.Rect
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.fast.ekyc.base.ui.BaseViewModel
import com.fast.ekyc.base.ui.SingleHandlerEvent
import com.fast.ekyc.base.ui.camera.CustomSize
import com.fast.ekyc.data.config.request.EkycConfig
import com.fast.ekyc.data.model.AdvanceImageData
import com.fast.ekyc.ui.face.face_capture.toLabelPose
import com.fast.ekyc.ui.main.MainViewModel
import com.fast.ekyc.utils.BitmapUtils
import com.fast.ekyc.utils.DataHolder
import com.fast.ekyc.utils.extension.isAdvancedMode
import kotlinx.coroutines.launch
import javax.inject.Inject

internal class FacePreviewViewModel @Inject constructor(
    private val config: EkycConfig,
) : BaseViewModel() {

    private val _uiFlowDoneEvent = MutableLiveData<SingleHandlerEvent<Unit>>()
    val uiFlowDoneEvent: LiveData<SingleHandlerEvent<Unit>> = _uiFlowDoneEvent

    private val _showErrorDialogEvent = MutableLiveData<SingleHandlerEvent<String>>()
    val showErrorDialogEvent: LiveData<SingleHandlerEvent<String>> = _showErrorDialogEvent

    private val _showHelp = MutableLiveData<Boolean>()
    val showHelp: LiveData<Boolean> = _showHelp

    private val _step = MutableLiveData<String>()
    val step: LiveData<String> = _step

    private lateinit var windowSize: CustomSize
    private lateinit var hole: Rect
    private var mainViewModel: MainViewModel? = null

    fun setupForCroppingBitmap(windowSize: CustomSize, hole: Rect) {
        this.windowSize = windowSize
        this.hole = hole
    }

    fun setMainViewModel(mainViewModel: MainViewModel) {
        this.mainViewModel = mainViewModel
    }

    override fun onCleared() {
        super.onCleared()
        mainViewModel = null
    }

    init {
        _showHelp.value = !config.isAdvancedMode() && config.showHelp
    }

    private fun handleUIOnly() {
        val faceBitmap = DataHolder.getFaceBitmap() ?: return
        viewModelScope.launch {
            showLoading()

            val bitmap = BitmapUtils.cropBitmap(windowSize, hole, faceBitmap) ?: return@launch
            saveLocalImage(bitmap)
            if (config.isAdvancedMode()) {
                val firstAdvance = mainViewModel?.firstAdvancedFace ?: return@launch
                val secondAdvance = mainViewModel?.secondAdvancedFace ?: return@launch

                val firstAdvanceBitmap = firstAdvance.image
                val secondAdvanceBitmap = secondAdvance.image

                DataHolder.uiOnlyAdvanceImageDataList = listOf(
                    AdvanceImageData(
                        localImage = firstAdvanceBitmap,
                        labelPose = firstAdvance.state.toLabelPose().value
                    ),
                    AdvanceImageData(
                        localImage = secondAdvanceBitmap,
                        labelPose = secondAdvance.state.toLabelPose().value
                    ),
                )
            }

            _uiFlowDoneEvent.postValue(SingleHandlerEvent(Unit))
            hideLoading()
        }
    }

    fun verifyImage() {
        handleUIOnly()
    }

    // Just Cache for straight Face
    private fun saveLocalImage(croppedBitmap: Bitmap) {
        // UI Only Flow
        DataHolder.uiFlowLocalFullImage = DataHolder.getFaceBitmap()
    }
}
