package com.fast.ekyc.base.ui.camera

import android.Manifest
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.YuvImage
import android.hardware.Camera
import android.hardware.Camera.CameraInfo
import android.hardware.Camera.Parameters.FLASH_MODE_OFF
import android.hardware.Camera.Parameters.FLASH_MODE_TORCH
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.Surface
import android.view.SurfaceHolder
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.lifecycleScope
import com.fast.ekyc.R
import com.fast.ekyc.base.ui.BaseFragment
import com.fast.ekyc.base.ui.BaseViewModel
import com.fast.ekyc.data.config.request.EkycConfig
import com.fast.ekyc.ui.widget.AutoFitSurfaceView
import com.fast.ekyc.utils.BrightnessManager
import com.fast.ekyc.utils.CameraHelper
import com.fast.ekyc.utils.extension.hasPermission
import com.fast.ekyc.utils.extension.onRequestPermissionsResult
import com.fast.ekyc.utils.extension.requestPermission
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.IOException


@Suppress("DEPRECATION")
internal abstract class BaseCameraFragment<T : ViewDataBinding, VM : BaseViewModel> :
    BaseFragment<T, VM>(), SurfaceHolder.Callback, Camera.PreviewCallback {

    private var isProcess = false
    private var startProcessTime = 0L
    protected var camera: Camera? = null
    protected var previewing = false
    protected var flashOn = false

    protected var needCaptureImage = false
    protected var videoPath: String? = null

    protected fun getAIRotatedBitmap(data: ByteArray, isScale: Boolean = false): Bitmap? {
        val parameters = try {
            camera?.parameters ?: return null
        } catch (e: Exception) {
            return null
        }
        val previewWidth: Int = parameters.previewSize.width
        val previewHeight: Int = parameters.previewSize.height
        val yuv = YuvImage(data, parameters.previewFormat, previewWidth, previewHeight, null)
        val out = ByteArrayOutputStream()
        yuv.compressToJpeg(Rect(0, 0, previewWidth, previewHeight), 100, out)
        val convertedData = out.toByteArray()
        val originalBitmap = BitmapFactory.decodeByteArray(convertedData, 0, convertedData.size)

        return getRotatedBitmap(originalBitmap, isScale)
    }

    private fun getRotatedBitmap(originalBitmap: Bitmap, isScale: Boolean = false): Bitmap {
        val context = context ?: return originalBitmap
        val display =
            (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay

        return if (display.rotation == Surface.ROTATION_0) {
            //rotate bitmap, because camera sensor usually in landscape mode
            val matrix = Matrix()
            val rotation =
                CameraHelper.getRotationCompensation(getCameraId(), requireActivity()).toFloat()
            matrix.postRotate(rotation)

            val rotatedBitmapWidth = originalBitmap.width
            val rotatedBitmapHeight = originalBitmap.height
            val scaled = minOf(MAX_AI_SIZE / maxOf(rotatedBitmapWidth, rotatedBitmapHeight), 1f)

            if (isFrontCamera()) {
                matrix.postScale(-1f, 1f, originalBitmap.width / 2f, originalBitmap.height / 2f)
            }
            if (isScale) {
                matrix.postScale(scaled, scaled)
            }
            Bitmap.createBitmap(
                originalBitmap,
                0,
                0,
                originalBitmap.width,
                originalBitmap.height,
                matrix,
                true
            )
        } else originalBitmap
    }

    private fun getCropByScreenBitmap(
        data: ByteArray,
    ): Bitmap? {
        val parameters = camera?.parameters ?: return null
        val previewWidth: Int = parameters.previewSize.width
        val previewHeight: Int = parameters.previewSize.height
        val yuv = YuvImage(data, parameters.previewFormat, previewWidth, previewHeight, null)
        val out = ByteArrayOutputStream()
        yuv.compressToJpeg(Rect(0, 0, previewWidth, previewHeight), 100, out)
        val convertedData = out.toByteArray()

        val originalBitmap = BitmapFactory.decodeByteArray(convertedData, 0, convertedData.size)
        val rotateBitmap = getRotatedBitmap(originalBitmap)
        val bitmapRatio = rotateBitmap.width.toFloat() / rotateBitmap.height

        val screenRatio = handleUpdateLayoutWithIllegalState {
            screenSize.getRatio()
        } ?: 1.0f

        return if (screenRatio < bitmapRatio) {
            val newWidth = rotateBitmap.height * screenRatio
            val delta = (rotateBitmap.width - newWidth) / 2f

            Bitmap.createBitmap(
                rotateBitmap,
                delta.toInt(),
                0,
                newWidth.toInt(),
                rotateBitmap.height
            )
        } else {
            val newHeight = rotateBitmap.width / screenRatio
            val delta = (rotateBitmap.height - newHeight) / 2f

            Bitmap.createBitmap(
                rotateBitmap,
                0,
                delta.toInt(),
                rotateBitmap.width,
                newHeight.toInt()
            )
        }
    }

    protected fun isFrontCamera() = getCameraId() == CameraInfo.CAMERA_FACING_FRONT

    override fun initComponents() {
        requestPermissionInStart()

        getFrameTextView().isVisible = getConfig().isDebug
    }

    private fun requestPermissionInStart() {
        if (hasPermission(Manifest.permission.CAMERA)) {
            onPermissionGranted()
        } else {
            requestPermission(Manifest.permission.CAMERA)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        onRequestPermissionsResult(
            requestCode = requestCode,
            permissions = permissions,
            grantResults = grantResults,
            onPermissionGranted = { onPermissionGranted() },
            onPermissionDenied = {
                context?.let {
                    AlertDialog.Builder(it)
                        .setTitle(R.string.kyc_notice)
                        .setMessage(R.string.kyc_camera_required_permission)
                        .setPositiveButton(R.string.kyc_camera_go_to_app_settings) { _, _ ->
                            goToAppSettings()
                        }
                        .setNegativeButton(R.string.kyc_cancel) { _, _ -> onPermissionDenied() }
                        .setCancelable(false)
                        .show()
                }
            },
        )
    }

    open fun onPermissionDenied() {
        getMainActivity()?.onCancelled()
    }

    private fun goToAppSettings() {
        val activity = activity ?: return

        val intent = Intent().apply {
            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            data = Uri.fromParts("package", activity.packageName, null)
        }
        activity.startActivity(intent)

        activity.finish()
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        camera?.release()
        camera = openCamera()

        lifecycleScope.launch(Dispatchers.Main) {
            delay(300)
            camera?.setPreviewCallback(this@BaseCameraFragment)
        }
    }

    private fun openCamera(): Camera? {
        val cameraInfo = CameraInfo()
        val cameraCount = Camera.getNumberOfCameras()

        if (cameraCount == 0) activity?.finish()

        var cameraIndex = -1
        for (camIdx in 0 until cameraCount) {
            Camera.getCameraInfo(camIdx, cameraInfo)
            if (cameraInfo.facing == getCameraId()) {
                cameraIndex = camIdx
                break
            }
        }

        try {
            if (cameraIndex == -1) {
                return Camera.open()
            }
            return Camera.open(cameraIndex)
        } catch (error: RuntimeException) {
            activity?.finish()
            return null
        }
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        val camera = camera ?: return
        try {
            val parameters = camera.parameters
            onFlashAvailable(parameters.flashMode != null)
            if (parameters.isZoomSupported) {
                parameters.zoom = getConfig().zoom
            }

            //find optimal - it very important
            val previewSizeOptimal = parameters.previewSize

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

    fun updateFlashMode(isOn: Boolean) {
        if (isFrontCamera()) {
            if (isOn) {
                BrightnessManager.turnOnFlash(requireActivity())
            } else {
                BrightnessManager.turnOffFlash(requireActivity())
            }

            return
        }

        val camera = camera ?: return
        try {
            val parameters = camera.parameters
            val flashModes = parameters.supportedFlashModes
            val flash = parameters.flashMode
            if (flashModes != null) {
                if (isOn && flashModes.contains(FLASH_MODE_TORCH) && flash != FLASH_MODE_TORCH) {
                    parameters.flashMode = FLASH_MODE_TORCH
                } else if (!isOn && flashModes.contains(FLASH_MODE_OFF) && flash != FLASH_MODE_OFF) {
                    parameters.flashMode = FLASH_MODE_OFF
                }
            }
            camera.parameters = parameters
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        camera?.setPreviewCallback(null)
        camera?.stopPreview()
        camera?.release()
        camera = null
        previewing = false
    }

    open fun releaseCamera() {
        if (camera != null) {
            camera!!.release() // release the camera for other applications
            camera = null
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        releaseCamera()
    }

    protected fun captureImage(data: ByteArray, shouldShowCaptureEffect: Boolean = true) {
        lifecycleScope.launch(Dispatchers.Main) {
            if (shouldShowCaptureEffect) {
                context?.let {
                    BrightnessManager.turnOnFlash(requireActivity())
                    delay(150)
                    BrightnessManager.turnOffFlash(requireActivity())
                }
            }

            getCropByScreenBitmap(data)?.let {
                onCapturedImage(it)
            }
        }
    }

    protected fun <T> handleUpdateLayoutWithIllegalState(function: (() -> T)): T? {
        return try {
            function.invoke()
        } catch (error: IllegalStateException) {
            null
        }
    }

    abstract fun onPermissionGranted()
    abstract fun onCapturedImage(bitmap: Bitmap)
    abstract fun getSurfaceHolder(): SurfaceHolder
    abstract fun getCameraId(): Int
    abstract fun onPreviewFrame(data: ByteArray?)
    abstract fun getSurfaceView(): AutoFitSurfaceView
    abstract fun getConfig(): EkycConfig
    abstract fun onFlashAvailable(isFlashAvailable: Boolean)
    abstract fun getFrameTextView(): TextView
    abstract fun getFlashView(): FrameLayout
    abstract fun setFlashMode()
    abstract val screenSize: CustomSize

    override fun onPreviewFrame(data: ByteArray?, camera: Camera?) {
        onPreviewFrame(data)
    }

    protected fun isProcessing() = isProcess

    protected fun startProcess() {
        startProcessTime = System.currentTimeMillis()
        this.isProcess = true
    }

    protected suspend fun endProcess(additional: String = "") {
        this.isProcess = false
        withContext(Dispatchers.Main) {
            setFrameTime(additional)
        }
    }

    private fun setFrameTime(additional: String = "") {
        if (!getConfig().isDebug) return
        try {
            val deltaTime = System.currentTimeMillis() - startProcessTime
            val displayTime = "${1 / (deltaTime / 1000f)} Fps $additional"
            getFrameTextView().text = displayTime
        } catch (error: IllegalStateException) {
        }
    }

    companion object {
        const val ASPECT_TOLERANCE = 0.05
        const val MAX_AI_SIZE = 640f
    }
}