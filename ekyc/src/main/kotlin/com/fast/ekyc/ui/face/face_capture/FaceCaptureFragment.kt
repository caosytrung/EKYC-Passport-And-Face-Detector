package com.fast.ekyc.ui.face.face_capture

import android.content.Context
import android.graphics.Bitmap
import android.graphics.RectF
import android.hardware.Camera
import android.hardware.Camera.CameraInfo
import android.os.Build
import android.os.Bundle
import android.view.Surface
import android.view.SurfaceHolder
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.fast.ekyc.BR
import com.fast.ekyc.R
import com.fast.ekyc.FastEkycSDK
import com.fast.ekyc.base.ui.SingleHandlerEventObserver
import com.fast.ekyc.base.ui.camera.BaseCameraFragment
import com.fast.ekyc.base.ui.camera.CustomSize
import com.fast.ekyc.data.config.request.EkycConfig
import com.fast.ekyc.databinding.KycFragmentFaceCaptureBinding
import com.fast.ekyc.native.NativeFunctionCall
import com.fast.ekyc.tracking.EventAction
import com.fast.ekyc.tracking.EventName
import com.fast.ekyc.tracking.EventSrc
import com.fast.ekyc.tracking.ObjectType
import com.fast.ekyc.ui.face.normal_face_guide.FaceGuideBottomSheet
import com.fast.ekyc.ui.result_popup.ImageErrorBottomSheet
import com.fast.ekyc.ui.state.CaptureState
import com.fast.ekyc.utils.CardIOUCaptureUtils
import com.fast.ekyc.utils.DataHolder
import com.fast.ekyc.utils.FaceIOUCaptureUtils
import com.fast.ekyc.utils.extension.isAdvancedMode
import com.fast.ekyc.utils.extension.setOnSingleClickListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject
import kotlin.math.abs


@Suppress("DEPRECATION")
internal class FaceCaptureFragment :
    BaseCameraFragment<KycFragmentFaceCaptureBinding, FaceCaptureViewModel>() {

    private val faceCaptureViewModel: FaceCaptureViewModel by viewModels { viewModelFactory }

    override fun getViewModel() = faceCaptureViewModel

    override fun getBindingVariable() = BR.viewModel

    override fun getLayoutId() = R.layout.kyc_fragment_face_capture

    private var isGuideOpened = false

    private val holeRatio: RectF? by lazy {
        try {
            viewDataBinding.overlayView.getRatioOfHole()
        } catch (error: IllegalStateException) {
            null
        }
    }

    @Inject
    internal lateinit var config: EkycConfig

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (config.showHelp && !config.isAdvancedMode()) {
            showHelp()
        }

        faceCaptureViewModel.setMainViewModel(mainViewModel)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        FastEkycSDK.tracker?.createEventAndTrack(
            objectName = EventName.FACE_CAPTURE_SHOW,
            eventSrc = EventSrc.APP,
            objectType = ObjectType.VIEW,
            action = EventAction.SHOW
        )
    }

    private fun showHelp(shouldDelay: Boolean = true) {
        lifecycleScope.launch {
            if (shouldDelay) {
                delay(200)
            }
            if (childFragmentManager.findFragmentByTag(FaceGuideBottomSheet.TAG) == null) {
                isGuideOpened = true
                FaceGuideBottomSheet.getInstance(config.isAdvancedMode()) {
                    isGuideOpened = false
                }.show(childFragmentManager, FaceGuideBottomSheet.TAG)
            }
        }
    }

    private fun showRetakeErrorAndExit() {
        faceCaptureViewModel.setFaceState(CaptureState.LIMITED)

        handleUpdateLayoutWithIllegalState {
            if (childFragmentManager.findFragmentByTag(ImageErrorBottomSheet.TAG) == null) {
                camera?.setPreviewCallback(null)
                val content =
                    if (config.isAdvancedMode()) context?.getString(R.string.kyc_please_view_guide_video)
                        ?: "" else ""
                val closeButtonText =
                    context?.getString(R.string.kyc_close) ?: ""

                val title = context?.getString(R.string.kyc_limited_time) ?: ""
                val bottomSheet = ImageErrorBottomSheet.newInstance(
                    title = title,
                    content = content,
                    closeButtonText
                ) {
                    getMainActivity()?.onCancelled()
                }

                bottomSheet.show(childFragmentManager, ImageErrorBottomSheet.TAG)
            }
        }
    }

    override fun initComponents() {
        FaceIOUCaptureUtils.reset()

        viewDataBinding.apply {
            overlayView.setFaceAdvanced(config.isAdvancedMode())
            tvPose.isVisible = getConfig().isDebug
            val surfaceHolder = cameraView.holder
            surfaceHolder.addCallback(this@FaceCaptureFragment)
            surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)

            lnCamera.setOnSingleClickListener { needCaptureImage = true }
            ivClose.setOnSingleClickListener {
                getMainActivity()?.onCancelled()
            }

            faceCaptureViewModel.resetState()

            if (config.isAdvancedMode()) {
                mainViewModel.resetCardRetake()
            }

            ivGuide.setOnSingleClickListener { showHelp(false) }

            val title = if (!config.isAdvancedMode()) {
                R.string.kyc_capture_face
            } else {
                R.string.kyc_record_face
            }
            tvTitle.setText(title)

            lnFlash.setOnSingleClickListener {
                flashOn = !flashOn
                setFlashMode()
            }
        }

        if (!config.isAdvancedMode()) {
            setFlashMode()
        }

        observeViewModel()
        super.initComponents()
    }

    override fun setFlashMode() {
        val drawableRes =
            if (flashOn) R.drawable.kyc_ic_baseline_flash_on_24
            else R.drawable.kyc_ic_baseline_flash_off_24
        viewDataBinding.ivFlash.setImageResource(drawableRes)

        updateFlashMode(flashOn)
    }

    private fun observeViewModel() {
        faceCaptureViewModel.apply {
            doneEvent.observe(viewLifecycleOwner, SingleHandlerEventObserver {
                handleDoneEvent()
            })

            limitTimeErrorEvent.observe(viewLifecycleOwner, SingleHandlerEventObserver {
                showLimitTimeError()
            })

            retakeEvent.observe(viewLifecycleOwner, SingleHandlerEventObserver {
                mainViewModel.decreaseFaceRetake()
            })
        }
    }

    private fun showLimitTimeError() {
        lifecycleScope.launch(Dispatchers.Main) {
            if (childFragmentManager.findFragmentByTag(ImageErrorBottomSheet.TAG) == null) {
                val title = "Đã thực hiện quá thời gian cho phép";
                camera?.setPreviewCallback(null)
                val bottomSheet = ImageErrorBottomSheet.newInstance(
                    title = title,
                    content = "Vui lòng thực hiện trong ${config.advancedLivenessConfig.duration} giây",
                    context?.getString(R.string.kyc_rerecord) ?: ""
                ) {
                    restart()
                }

                bottomSheet.show(childFragmentManager, ImageErrorBottomSheet.TAG)
            }
        }
    }

    private fun restart() {
        CardIOUCaptureUtils.reset()
        lifecycleScope.launch {
            faceCaptureViewModel.resetState()
            camera?.setPreviewCallback(this@FaceCaptureFragment)
        }
    }

    private fun handleDoneEvent() {
        camera?.setPreviewCallback(null)
        showLoading()
        lifecycleScope.launch(Dispatchers.Main) {
            showLoading()
            hideLoading()
            moveToImageVerification()
        }
    }

    override fun onPermissionGranted() {}

    private fun moveToImageVerification() {
        findNavController().navigate(
            FaceCaptureFragmentDirections.kycActionKycFacecapturefragmentToKycFacepreviewfragment()
        )
    }

    override fun getSurfaceView() = viewDataBinding.cameraView

    override fun onCapturedImage(bitmap: Bitmap) {
        DataHolder.setFaceBitmap(bitmap)

        if (!config.isAdvancedMode()) {
            moveToImageVerification()
        }
    }

    override val screenSize: CustomSize by lazy {
        CustomSize(viewDataBinding.overlayView.width, viewDataBinding.overlayView.height)
    }

    override fun getSurfaceHolder(): SurfaceHolder {
        return viewDataBinding.cameraView.holder
    }

    override fun getConfig() = config

    override fun getFrameTextView() = viewDataBinding.tvFrameTime

    override fun getCameraId(): Int {
        if (config.selfieCameraMode.isBack()) return CameraInfo.CAMERA_FACING_BACK

        val numberOfCameras = Camera.getNumberOfCameras()
        for (i in 0 until numberOfCameras) {
            val cameraInfo = CameraInfo()
            Camera.getCameraInfo(i, cameraInfo)
            if (cameraInfo.facing == CameraInfo.CAMERA_FACING_FRONT) {
                return CameraInfo.CAMERA_FACING_FRONT
            }
        }
        return CameraInfo.CAMERA_FACING_BACK
    }

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
        if (data == null || isGuideOpened) return

        var bitmapWithSize: Triple<Bitmap, CustomSize, Float>? = null

        if (needCaptureImage) {
            captureImage(data, false)
            needCaptureImage = false
            if (!config.isAdvancedMode()) {
                camera?.setPreviewCallback(null)
            }
            return
        }

        if (isProcessing()) return
        if (mainViewModel.isFaceRetakeLimited()) {
            camera?.setPreviewCallback(null)
            showRetakeErrorAndExit()
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            startProcess()
            if (bitmapWithSize == null) {
                bitmapWithSize = getFaceAiBitmap(data) ?: return@launch
            }

            if (config.isAdvancedMode()) {
                handleAdvancedMode(bitmapWithSize!!, data)
            } else {
                handleNormalMode(bitmapWithSize!!, data)
            }

            withContext(Dispatchers.Main) {
                endProcess()
            }
        }
    }

    private suspend fun handleNormalMode(
        bitmapWithSize: Triple<Bitmap, CustomSize, Float>,
        data: ByteArray,
    ) {
        val bitmap = bitmapWithSize.first
        val screenSizeByBitmap = bitmapWithSize.second
        val deltaTop = bitmapWithSize.third
        val hole = getHole(screenSizeByBitmap)

        val result = NativeFunctionCall.detectPose(bitmap)
        if (result.size != 1 && result.size != 8) {
            faceCaptureViewModel.setFaceState(CaptureState.UNKNOWN)
            return
        }

        if (result.size == 1) {
            val isEmptyFace = result[0] == 0f
            if (isEmptyFace) {
                faceCaptureViewModel.setFaceState(CaptureState.UNKNOWN)
            } else {
                faceCaptureViewModel.setFaceState(CaptureState.TOO_MANY_OBJECT)
            }

            return
        }

        val yaw = result[0]
        val x = result[3]
        val y = result[4] + deltaTop
        val width = (result[5])
        val height = result[6]
        val confidence = result[7]
        val faceBottom = y + height
        val box = RectF(x, y, x + width, faceBottom)

        if (config.isDebug) {
            handleUpdateLayoutWithIllegalState {
                viewDataBinding.overlayView.drawFaceBox(
                    box,
                    screenSizeByBitmap
                )
            }
        }

        val faceRatio = width / hole.width()
        val imagePoseState = getPoseState(yaw)
        val isContain = hole.contains(box)

        if (!isContain) {
            faceCaptureViewModel.setFaceState(CaptureState.UNKNOWN)
            return
        }

        if (imagePoseState != AdvancedFaceState.PORTRAIT) {
            faceCaptureViewModel.setFaceState(CaptureState.NO_PORTRAIT)
            return
        }

        if (faceRatio < config.faceMinRatio) {
            faceCaptureViewModel.setFaceState(CaptureState.TOO_SMALL)
            return
        }

        if (faceRatio > config.faceMaxRatio) {
            faceCaptureViewModel.setFaceState(CaptureState.TOO_BIG)
            return
        }

        faceCaptureViewModel.setFaceState(CaptureState.VALID)
        if (faceCaptureViewModel.autoCapture.value != false && confidence > 0.85) {
            if (FaceIOUCaptureUtils.checkValidFrame(box)) {
                withContext(Dispatchers.Main) {
                    camera?.setPreviewCallback(null)
                    captureImage(data)
                }
            }
        }
    }

    private suspend fun handleAdvancedMode(
        bitmapWithSize: Triple<Bitmap, CustomSize, Float>,
        data: ByteArray,
    ) {
        val bitmap = bitmapWithSize.first
        val screenSizeByBitmap = bitmapWithSize.second
        val deltaTop = bitmapWithSize.third

        withContext(Dispatchers.Main) {
            val result = NativeFunctionCall.detectPose(bitmap)

            if (result.size != 1 && result.size != 8) {
                faceCaptureViewModel.setFaceState(CaptureState.UNKNOWN)
                return@withContext
            }

            if (result.size == 1) {
                val isEmptyFace = result[0] == 0f
                if (isEmptyFace) {
                    faceCaptureViewModel.setFaceState(CaptureState.UNKNOWN)
                } else {
                    faceCaptureViewModel.setFaceState(CaptureState.TOO_MANY_OBJECT)
                }

                return@withContext
            }

            // get pose, pose = {yaw, pitch, roll}
            val yaw = result[0]
            val pitch = result[1]
            val x = result[3]
            val y = result[4] + deltaTop
            val width = (result[5])
            val height = result[6]
            val confidence = result[7]
            val faceBottom = y + height

            val box = RectF(x, y, x + width, faceBottom)

            if (config.isDebug) {
                handleUpdateLayoutWithIllegalState {
                    viewDataBinding.overlayView.drawFaceBox(
                        box,
                        screenSizeByBitmap,
                    )
                }
            }

            val calculatingHole = getHole(screenSizeByBitmap)
            val faceRatio = width / calculatingHole.width()

            if (faceCaptureViewModel.faceState.value?.isCardInsideFrame() == false) {
                if (faceRatio < config.faceMinRatio) {
                    faceCaptureViewModel.setFaceState(CaptureState.TOO_SMALL)
                    return@withContext
                }
                if (faceRatio > config.faceMaxRatio) {
                    faceCaptureViewModel.setFaceState(CaptureState.TOO_BIG)
                    return@withContext
                }
            }

            val shouldCaptureStraightFace = !faceCaptureViewModel.isStraightAdvancedCaptured()

            if (confidence < 0.6) {
                faceCaptureViewModel.setFaceState(CaptureState.UNKNOWN)
                return@withContext
            }

            val isContain = calculatingHole.contains(box)
            if (!isContain) {
                faceCaptureViewModel.setFaceState(CaptureState.MOVE_OUT_OF_FRAME)
                return@withContext
            }

            val imagePoseState = getPoseState(yaw)

            if (shouldCaptureStraightFace) {
                if (FaceIOUCaptureUtils.checkValidFrame(box)) {
                    withContext(Dispatchers.Main) {
                        captureImage(data, false)
                    }

                    faceCaptureViewModel.setFaceState(CaptureState.VALID)
                    faceCaptureViewModel.setAdvancedCaptured(true)
                } else {
                    faceCaptureViewModel.setFaceState(CaptureState.KEEP_STRAIGHT)
                }

                return@withContext
            }

            faceCaptureViewModel.setFaceState(CaptureState.VALID)

            val currentAction = faceCaptureViewModel.getCurrentAction()
            val percent = if (currentAction == AdvancedFaceState.LEFT) {
                val leftPercent = if (yaw < 0) {
                    0
                } else if (imagePoseState == AdvancedFaceState.LEFT) {
                    100
                } else {
                    (yaw * 100 / config.advancedLivenessConfig.leftAngle).toInt()
                }
                leftPercent
            } else {
                val rightPercent = if (yaw > 0) {
                    0
                } else if (imagePoseState == AdvancedFaceState.RIGHT) {
                    100
                } else {
                    abs((yaw * 100 / config.advancedLivenessConfig.rightAngle).toInt())
                }
                rightPercent
            }
            faceCaptureViewModel.setAdvancedPercent(percent)

            val index = faceCaptureViewModel.getCurrentIndex()
            when (currentAction) {
                AdvancedFaceState.LEFT -> {
                    if (imagePoseState == AdvancedFaceState.LEFT) {
                        faceCaptureViewModel.nextAction()
                        mainViewModel.setAdvanceFace(index, bitmap, AdvancedFaceState.LEFT)
                    }
                }

                AdvancedFaceState.RIGHT -> {
                    if (imagePoseState == AdvancedFaceState.RIGHT) {
                        faceCaptureViewModel.nextAction()
                        mainViewModel.setAdvanceFace(index, bitmap, AdvancedFaceState.RIGHT)
                    }
                }

                else -> {}
            }
        }
    }

    private fun getPoseState(yaw: Float): AdvancedFaceState {
        if (yaw > config.advancedLivenessConfig.leftAngle) {
            return AdvancedFaceState.LEFT
        }

        if (yaw < -config.advancedLivenessConfig.rightAngle) {
            return AdvancedFaceState.RIGHT
        }

        return AdvancedFaceState.PORTRAIT
    }

    private fun getFaceAiBitmap(data: ByteArray): Triple<Bitmap, CustomSize, Float>? {
        val rotateBitmap = getAIRotatedBitmap(data, true) ?: return null

        val screenRatio = handleUpdateLayoutWithIllegalState {
            screenSize.getRatio()
        } ?: 1.0f

        var aiBitmapWidth = (rotateBitmap.height * screenRatio).toInt()
        var deltaX = (rotateBitmap.width - aiBitmapWidth) / 2f
        if (deltaX < 0) {
            deltaX = 0f
            aiBitmapWidth = rotateBitmap.width
        }

        val screenSizeByBitmap = CustomSize(aiBitmapWidth, rotateBitmap.height)

        val holeRatio = holeRatio ?: RectF()
        val frameTop = (holeRatio.top * screenSizeByBitmap.height).toInt()
        val frameHeight = (holeRatio.height() * screenSizeByBitmap.height).toInt()
        val deltaY = (frameHeight * 0.06f).toInt()
        val top = maxOf(0, frameTop - deltaY)

        val croppedBitmap = Bitmap.createBitmap(
            rotateBitmap,
            deltaX.toInt(),
            top,
            aiBitmapWidth,
            minOf(screenSizeByBitmap.height, frameHeight + deltaY * 2)
        )

        return Triple(croppedBitmap, screenSizeByBitmap, top.toFloat())
    }

    override fun onFlashAvailable(isFlashAvailable: Boolean) {
        if (!isFlashAvailable && !isFrontCamera()) {
            viewDataBinding.lnFlash.isVisible = false
        }
    }

    override fun getFlashView(): FrameLayout = viewDataBinding.flashEmulator

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        val camera = camera ?: return
        try {
            val parameters = camera.parameters
            onFlashAvailable(parameters.flashMode != null)
            if (parameters.isZoomSupported) {
                parameters.zoom = getConfig().zoom
            }

            //get preview sizes
            val previewSizes = parameters.supportedPreviewSizes

            //find optimal - it very important
            val previewSizeOptimal = getOptimalPreviewSize(previewSizes)

            previewSizeOptimal?.let {
                getSurfaceView().setAspectRatio(it.width, it.height)
                parameters.setPreviewSize(it.width, it.height)
            }

            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
                if (camera.parameters.focusMode.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                    parameters.focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE
                }
            }

            if (camera.parameters.flashMode?.contains(Camera.Parameters.FLASH_MODE_AUTO) == true) {
                parameters.flashMode = Camera.Parameters.FLASH_MODE_AUTO
            }

            camera.parameters = parameters

            //rotate screen, because camera sensor usually in landscape mode
            val context = context ?: return
            val display =
                (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
            if (display.rotation == Surface.ROTATION_0) {
                camera.setDisplayOrientation(90)
            } else if (display.rotation == Surface.ROTATION_270) {
                camera.setDisplayOrientation(180)
            }

            //write some info
            camera.setPreviewDisplay(getSurfaceHolder())
            camera.startPreview()
            previewing = true
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun getOptimalPreviewSize(sizes: List<Camera.Size>): Camera.Size? {
        val targetRatio = 0.75f
        val sizeMap = mutableMapOf<Float, Camera.Size>()

        for (size in sizes) {
            val width = size.width.toFloat()
            val height = size.height

            val ratio = if (width > height) height / width else width / height
            if (ratio + ASPECT_TOLERANCE >= targetRatio) {
                if (sizeMap.containsKey(ratio)) {
                    val oldSize = sizeMap[ratio] ?: continue
                    if (oldSize.width <= width) {
                        sizeMap[ratio] = size
                    }
                } else {
                    sizeMap[ratio] = size
                }
            }
        }

        if (sizeMap.isEmpty()) {
            return null
        }

        val smallestRatio = sizeMap.keys.minOrNull() ?: return null

        return sizeMap[smallestRatio]
    }
}