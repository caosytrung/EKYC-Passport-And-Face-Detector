package com.fast.ekyc.ui.main

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.navigation.findNavController
import com.fast.ekyc.BR
import com.fast.ekyc.R
import com.fast.ekyc.base.ui.BaseActivity
import com.fast.ekyc.data.config.request.EkycConfig
import com.fast.ekyc.data.model.KycUIFlowResult
import com.fast.ekyc.data.model.ResultState
import com.fast.ekyc.databinding.KycActivityMainBinding
import com.fast.ekyc.di.component.DaggerKycComponent
import com.fast.ekyc.native.NativeFunctionCall
import com.fast.ekyc.utils.FileUtils
import com.fast.ekyc.utils.checksum.DeviceUUIdUtils
import com.fast.ekyc.utils.extension.hasPermission
import com.fast.ekyc.utils.extension.onRequestPermissionsResult
import com.fast.ekyc.utils.extension.requestPermissions
import com.fast.ekyc.utils.extension.shouldOpenFaceIdentification


internal class MainActivity : BaseActivity<KycActivityMainBinding, MainViewModel>() {

    private val mainViewModel: MainViewModel by viewModels {
        viewModelFactory
    }

    private val navController by lazy { findNavController(R.id.main_content) }

    override fun getViewModel() = mainViewModel

    override fun getBindingVariable() = BR.viewModel

    override fun getLayoutId() = R.layout.kyc_activity_main

    private val config: EkycConfig by lazy {
        intent?.getParcelableExtra<EkycConfig>(EKYC_CONFIG_KEY)
            ?: throw IllegalArgumentException()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DeviceUUIdUtils.configUuidIfNeeded(this)
        FileUtils.copyAssets(this)?.also {
            NativeFunctionCall.init(assets, it)
        }

        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
    }

    override fun performDependencyInjection() {
        DaggerKycComponent
            .builder()
            .context(applicationContext)
            .config(config)
            .build()
            .injectMainActivity(this)
    }

    override fun initComponents() {
        requestPermissionInStart()
    }

    private fun requestPermissionInStart() {

        var isStoreGranted = true
        val isCameraGranted = hasPermission(Manifest.permission.CAMERA)

        if (isCameraGranted && isStoreGranted) {
            onPermissionGranted()
        } else {
            val requestPermissions = mutableListOf<String>()
            if (!isCameraGranted) {
                requestPermissions.add(Manifest.permission.CAMERA)
            }

            if (!isStoreGranted) {
                requestPermissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }

            requestPermissions(requestPermissions)
        }
    }

    private fun onPermissionGranted() {
        // Configure the navigation
        val graph = navController.navInflater.inflate(R.navigation.kyc_main_navigation)
        val startDestination = if (config.shouldOpenFaceIdentification()) {
            R.id.kyc_facecapturefragment
        } else {
            R.id.kyc_cardcapturefragment
        }
        graph.setStartDestination(startDestination)
        navController.graph = graph
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
                AlertDialog.Builder(this)
                    .setTitle(R.string.kyc_notice)
                    .setMessage(R.string.kyc_camera_required_permission)
                    .setPositiveButton(R.string.kyc_camera_go_to_app_settings) { _, _ ->
                        goToAppSettings()
                    }
                    .setNegativeButton(R.string.kyc_cancel) { _, _ -> onPermissionDenied() }
                    .setCancelable(false)
                    .show()
            },
        )
    }

    private fun onPermissionDenied() {
        onCancelled()
    }

    private fun goToAppSettings() {
        val intent = Intent().apply {
            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            data = Uri.fromParts("package", packageName, null)
        }

        startActivity(intent)
        finish()
    }

    fun onUIFlowDone() {
        val internalKycResult =
            KycUIFlowResult(
                resultState = ResultState.Success,
                imageId = mainViewModel.uiFlowImageId,
                videoPath = mainViewModel.videoPath
            )

        val intent = Intent()
        intent.putExtra(RESULT_DATA, internalKycResult)
        setResult(RESULT_OK, intent)
        finish()
    }

    fun onCancelled() {
        val internalKycResult = KycUIFlowResult(resultState = ResultState.UserCancelled)
        val intent = Intent()
        intent.putExtra(RESULT_DATA, internalKycResult)
        setResult(RESULT_OK, intent)
        finish()
    }

    companion object {
        private const val EKYC_CONFIG_KEY = "EKYC_CONFIG_KEY"
        internal const val RESULT_DATA = "RESULT_DATA"

        fun buildIntent(context: Context, config: EkycConfig): Intent {
            return Intent(context, MainActivity::class.java).apply {
                putExtra(EKYC_CONFIG_KEY, config)
            }
        }
    }
}