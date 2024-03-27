package com.fast.ekyc.ui.card.card_capture

import android.graphics.Bitmap
import android.graphics.RectF
import android.hardware.Camera
import android.os.Bundle
import android.util.Log
import android.view.SurfaceHolder
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.fast.ekyc.BR
import com.fast.ekyc.FastEkycSDK
import com.fast.ekyc.R
import com.fast.ekyc.base.ui.camera.BaseCameraFragment
import com.fast.ekyc.base.ui.camera.CustomSize
import com.fast.ekyc.data.config.request.EkycConfig
import com.fast.ekyc.databinding.KycFragmentCardCaptureBinding
import com.fast.ekyc.native.NativeFunctionCall
import com.fast.ekyc.native.model.CardObject
import com.fast.ekyc.tracking.EventAction
import com.fast.ekyc.tracking.EventName
import com.fast.ekyc.tracking.EventSrc
import com.fast.ekyc.tracking.ObjectType
import com.fast.ekyc.ui.card.card_guide_popup.CardGuideBottomSheet
import com.fast.ekyc.ui.state.CaptureState
import com.fast.ekyc.ui.widget.overlay.CardCameraOverlayView
import com.fast.ekyc.utils.BitmapUtils.scaled
import com.fast.ekyc.utils.CardIOUCaptureUtils
import com.fast.ekyc.utils.DataHolder
import com.fast.ekyc.utils.extension.area
import com.fast.ekyc.utils.extension.isPassportOnly
import com.fast.ekyc.utils.extension.setOnSingleClickListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

internal class CardCaptureFragment :
    BaseCameraFragment<KycFragmentCardCaptureBinding, CardCaptureViewModel>() {

    private val cardCaptureViewModel: CardCaptureViewModel by viewModels { viewModelFactory }

    override fun getViewModel() = cardCaptureViewModel

    override fun getBindingVariable() = BR.viewModel

    override fun getLayoutId() = R.layout.kyc_fragment_card_capture

    private var isGuideOpened = false

    @Inject
    internal lateinit var config: EkycConfig

    private var isFirstTimeObserveTracking = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isFirstTimeObserveTracking = config.autoCaptureMode
        cardCaptureViewModel.setMainViewModel(mainViewModel)

        if (config.showHelp && cardCaptureViewModel.isFrontSide()) {
            openGuidePopup()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        FastEkycSDK.tracker?.createEventAndTrack(
            objectName = EventName.CARD_CAPTURE_SHOW,
            eventSrc = EventSrc.APP,
            objectType = ObjectType.VIEW,
            action = EventAction.SHOW
        )
    }

    private val holeRatio: RectF? by lazy {
        try {
            viewDataBinding.overlayView.getRatioOfHole()
        } catch (error: IllegalStateException) {
            null
        }
    }


    override fun initComponents() {
        CardIOUCaptureUtils.reset()

        viewDataBinding.apply {
            val surfaceHolder = cameraView.holder
            surfaceHolder.addCallback(this@CardCaptureFragment)
            surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)
            ivClose.setOnSingleClickListener {
                getMainActivity()?.onCancelled()
            }
            ivGuide.setOnSingleClickListener {
                openGuidePopup()
            }

            lnCamera.setOnSingleClickListener {
                needCaptureImage = true
            }

            lnFlash.setOnSingleClickListener {
                flashOn = !flashOn
                setFlashMode()
            }
        }

        cardCaptureViewModel.displayCardSide.observe(viewLifecycleOwner) {
            viewDataBinding.tvCameraGuide.cardSide = it
        }

        cardCaptureViewModel.autoCapture.observe(
            viewLifecycleOwner
        ) {
            if (it == isFirstTimeObserveTracking) {
                return@observe
            }

            isFirstTimeObserveTracking = it

            FastEkycSDK.tracker?.createEventAndTrack(
                objectName = EventName.CARD_CAPTURE_SWITCH_AUTO_CAPTURE,
                eventSrc = EventSrc.USER,
                objectType = ObjectType.BUTTON,
                action = EventAction.TAP,
                eventValue = mapOf(
                    "TYPE" to if (it) "on" else "off",
                )
            )
        }

        super.initComponents()
    }

    override fun setFlashMode() {
        val drawableRes =
            if (flashOn) R.drawable.kyc_ic_baseline_flash_on_24
            else R.drawable.kyc_ic_baseline_flash_off_24
        viewDataBinding.ivFlash.setImageResource(drawableRes)

        updateFlashMode(flashOn)
    }

    override val screenSize: CustomSize by lazy {
        CustomSize(viewDataBinding.parentView.width, viewDataBinding.parentView.height)
    }

    private fun moveToImageVerification() {
        hideLoading()

        findNavController().navigate(
            CardCaptureFragmentDirections.kycActionKycCardcapturefragmentToKycCardpreviewfragment(
                cardCaptureViewModel.getAiCardType()
            )
        )
    }

    private fun openGuidePopup() {
        if (childFragmentManager.findFragmentByTag(CardGuideBottomSheet.TAG) == null) {
            isGuideOpened = true
            CardGuideBottomSheet.getInstance(config.isPassportOnly()) {
                isGuideOpened = false
            }.show(childFragmentManager, CardGuideBottomSheet.TAG)

        }
    }

    override fun onPermissionGranted() {

    }

    override fun onCapturedImage(bitmap: Bitmap) {
        DataHolder.setCardBitmap(bitmap)
        moveToImageVerification()
    }

    override fun getSurfaceHolder(): SurfaceHolder {
        return viewDataBinding.cameraView.holder
    }

    override fun getCameraId() : Int {
        val cameraMode =
            if (config.idCardCameraMode.isBack()) Camera.CameraInfo.CAMERA_FACING_BACK else Camera.CameraInfo.CAMERA_FACING_FRONT

        val numberOfCameras = Camera.getNumberOfCameras()
        for (i in 0 until numberOfCameras) {
            val cameraInfo = Camera.CameraInfo()
            Camera.getCameraInfo(i, cameraInfo)
            if (cameraInfo.facing == cameraMode) {
                return cameraMode
            }
        }
        return Camera.CameraInfo.CAMERA_FACING_BACK
    }

    override fun getSurfaceView() = viewDataBinding.cameraView

    private fun getHole(size: CustomSize): RectF {
        try {
            val holeRatio = holeRatio ?: return RectF()
            return RectF(
                holeRatio.left * size.width,
                holeRatio.top * size.height,
                holeRatio.right * size.width,
                holeRatio.bottom * size.height,
            )
        } catch (error: IllegalStateException) {
            return RectF()
        }
    }

    override fun onPreviewFrame(data: ByteArray?) {
        if (isGuideOpened) return
        if (data == null) return

        if (needCaptureImage) {
            captureImage(data, false)
            needCaptureImage = false
            camera?.setPreviewCallback(null)
            return
        }

        if (isProcessing()) return

        lifecycleScope.launch(Dispatchers.IO) {
            startProcess()
            getCardAiBitmap(data)?.let {
                val bitmap = it.first
                val screenSize = it.second
                val result = NativeFunctionCall.detectCard(bitmap)

                withContext(Dispatchers.Main) {
                    handleCardResult(result, bitmap, screenSize, data)
                    endProcess("CardType: ${result.firstOrNull()?.className ?: "Undefined"}")
                }
            }
        }
    }

    private fun getCardAiBitmap(data: ByteArray): Pair<Bitmap, CustomSize>? {
        val rotatedBitmap = getAIRotatedBitmap(data) ?: return null

        val screenRatio = handleUpdateLayoutWithIllegalState {
            screenSize.getRatio()
        } ?: 1.0f

//        val aiBitmapWidth = (scaledBitmap.height * screenRatio).toInt()
//        val deltaX = (scaledBitmap.width - aiBitmapWidth) / 2f

        var aiBitmapWidth = (rotatedBitmap.height * screenRatio).toInt()
        var deltaX = (rotatedBitmap.width - aiBitmapWidth) / 2f
        if (deltaX < 0) {
            deltaX = 0f
            aiBitmapWidth = rotatedBitmap.width
        }

        val screenSizeByBitmap = CustomSize(aiBitmapWidth, rotatedBitmap.height)

        val aiBitmapHeight = (aiBitmapWidth / CardCameraOverlayView.CARD_RATIO).toInt()
        val deltaY = (rotatedBitmap.height - aiBitmapHeight) / 2f

        val croppedBitmap = Bitmap.createBitmap(
            rotatedBitmap,
            deltaX.toInt(),
            deltaY.toInt(),
            aiBitmapWidth,
            aiBitmapHeight
        )

//        else {
//            val aiBitmapHeight = (scaledBitmap.width / screenRatio).toInt()
//            val deltaY = (scaledBitmap.height - aiBitmapHeight) / 2f
//            val screenSizeBy = CustomSize(scaledBitmap.width, aiBitmapHeight)
//
//            val aiBitmapWidth = (aiBitmapHeight * CardCameraOverlayView.CARD_RATIO).toInt()
//            val deltaX = (scaledBitmap.width - aiBitmapWidth) / 2f
//
//            Bitmap.createBitmap(
//                scaledBitmap,
//                deltaX.toInt(),
//                deltaY.toInt(),
//                aiBitmapWidth,
//                aiBitmapHeight
//            ) to screenSizeBy
//        }

        val rotatedBitmapWidth = croppedBitmap.width
        val rotatedBitmapHeight = croppedBitmap.height

        val scaled = minOf(MAX_AI_SIZE / maxOf(rotatedBitmapWidth, rotatedBitmapHeight), 1f)
        screenSizeByBitmap.scaleSmaller(scaled)
        return croppedBitmap.scaled(scaled, true) to screenSizeByBitmap
    }

    private fun handleCardResult(
        cards: Array<CardObject>,
        bitmap: Bitmap,
        screenSizeByBitmap: CustomSize,
        data: ByteArray
    ) {
        val idCardBoxPercentage = 0.025f
        if (cards.size != 1) {
            cardCaptureViewModel.setCardState(CaptureState.UNKNOWN)
            return
        }
        val card = cards.first()
        val hole = getHole(screenSizeByBitmap)

        val deltaY = (screenSizeByBitmap.height - bitmap.height) / 2
        val originalCard = RectF(
            card.left,
            card.top + deltaY,
            card.right,
            card.bottom + deltaY,
        )

        if (config.isDebug) {
            handleUpdateLayoutWithIllegalState {
                viewDataBinding.overlayView.drawCardBox(
                    originalCard,
                    screenSizeByBitmap,
                    idCardBoxPercentage
                )
            }
        }

        val cardType = card.toIdCardTypeAndSide()
        if (!cardCaptureViewModel.isValidCardType(cardType)) {
            cardCaptureViewModel.setCardState(CaptureState.WRONG_TYPE)
            return
        }

        val minConfidence = if (card.isShouldLowConfidence()) 0.6 else 0.7

        if (card.confidence < minConfidence) {
            cardCaptureViewModel.setCardState(CaptureState.UNKNOWN)
            return
        }

        val cardDeltaWidth = originalCard.width() * idCardBoxPercentage
        val cardDeltaHeight = originalCard.height() * idCardBoxPercentage
        val containCardShape = RectF(
            originalCard.left + cardDeltaWidth,
            originalCard.top + cardDeltaHeight,
            originalCard.right - cardDeltaWidth,
            originalCard.bottom - cardDeltaHeight,
        )

        if (!hole.contains(containCardShape)) {
            cardCaptureViewModel.setCardState(CaptureState.UNKNOWN)
            return
        }


        val cardArea = originalCard.area()
        val holeArea = hole.area()

        Log.d("CardArea Hole care", "$cardArea-$holeArea")

        // idCardMinRatio = 0.6f
        if (holeArea * (0.6f) > cardArea) {
            cardCaptureViewModel.setCardState(CaptureState.TOO_SMALL)
            return
        }

        cardCaptureViewModel.setCardState(CaptureState.VALID)
        if (cardCaptureViewModel.autoCapture.value != false) {
            if (CardIOUCaptureUtils.checkValidFrame(originalCard)) {
                camera?.setPreviewCallback(null)
                captureImage(data)
            }
        }
    }

    override fun onFlashAvailable(isFlashAvailable: Boolean) {
        if (!isFlashAvailable && !isFrontCamera()) {
            viewDataBinding.lnFlash.isVisible = false
        }
    }

    override fun getConfig() = config
    override fun getFrameTextView() = viewDataBinding.tvFrameTime
    override fun getFlashView() = viewDataBinding.flashEmulator

}

