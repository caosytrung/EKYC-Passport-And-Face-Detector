package com.fast.ekyc.example

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.preference.PreferenceManager.getDefaultSharedPreferences
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.databinding.DataBindingUtil
import com.fast.ekyc.FastEkycSDK
import com.fast.ekyc.data.config.request.AdvancedLivenessConfig
import com.fast.ekyc.data.config.request.EkycConfig
import com.fast.ekyc.data.config.request.EkycConfigBuilder
import com.fast.ekyc.example.R
import com.fast.ekyc.example.databinding.ActivityMainBinding
import com.fast.ekyc.tracking.EkycTracking
import com.fast.ekyc.tracking.EventAction
import com.fast.ekyc.tracking.EventSrc
import com.fast.ekyc.tracking.ObjectType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class ExampleActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var currentUIFlowType: EkycConfig.UiFlowType = EkycConfig.UiFlowType.ID_CARD_FRONT
    private var currentTypeList: MutableSet<EkycConfig.IdCardType> =
        mutableSetOf(EkycConfig.IdCardType.CMND, EkycConfig.IdCardType.CCCD)

    private val uiFlowTypelist = EkycConfig.UiFlowType.values().map { it.name }
    private val idCardTypeList =
        EkycConfig.IdCardType.values().map { it.name }
            .map { if (it == "HC") "Hộ Chiếu" else it }

    private var trackingContent: String = ""
    private val uiFlowTypeAdapter by lazy {
        ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            uiFlowTypelist
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.btnKYC.setOnClickListener { startKyc() }

        binding.common.spUIFlowType.apply {
            adapter = uiFlowTypeAdapter
            onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long,
                    ) {
                        currentUIFlowType = EkycConfig.UiFlowType.valueOf(uiFlowTypelist[position])
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                }

            setSelection(1)
        }

        binding.card.btnSelectIdTypes.setOnClickListener {
            val selectedString =
                currentTypeList.map { it.name }.map { if (it == "HC") "Hộ Chiếu" else it }
            val selectedBooleans = idCardTypeList.map { selectedString.contains(it) }
            AlertDialog.Builder(this)
                .setTitle("Chọn loại giấy tờ")
                .setMultiChoiceItems(
                    idCardTypeList.toTypedArray(),
                    selectedBooleans.toBooleanArray()
                ) { _, which, isChecked ->
                    val name =
                        if (idCardTypeList[which] == "Hộ Chiếu") "HC" else idCardTypeList[which]
                    val item = EkycConfig.IdCardType.valueOf(name)
                    if (isChecked) {
                        currentTypeList.add(item)
                    } else {
                        currentTypeList.remove(item)
                    }
                }
                .setPositiveButton("Ok") { _, _ ->

                }
                .setCancelable(false)
                .show()
        }

        binding.card.swFrontCard.setOnCheckedChangeListener { _, isChecked ->
            binding.card.swFrontCard.text = if (isChecked) "Back" else "Front"
        }

        binding.face.swFrontFace.setOnCheckedChangeListener { _, isChecked ->
            binding.face.swFrontFace.text = if (isChecked) "Front" else "Back"
        }

        getPersistData(UI_FLOW_KEY).let {
            if (it.isNotEmpty()) {
                binding.common.spUIFlowType.setSelection(uiFlowTypelist.indexOf(it))
            }
        }

    }

    private fun startKyc() {
        try {
            val advancedRetake =
                binding.advanced.edtChallengeRetake.text.toString().toIntOrNull() ?: 4
            val leftAngle = binding.advanced.edtLeftAngle.text.toString().toIntOrNull() ?: 30
            val rightAngle = binding.advanced.edtRightAngle.text.toString().toIntOrNull() ?: 30

            val duration = binding.advanced.edtAdvancedDuration.text.toString()
                .toIntOrNull() ?: 20
            val advancedLivenessConfig = AdvancedLivenessConfig(
                leftAngle = leftAngle,
                rightAngle = rightAngle,
                duration = duration,
                challengeRetakeLimit = advancedRetake,
            )

            val backgroundColor: Int =
                if (binding.common.swBackground.isChecked) Color.parseColor("#66000000") else Color.YELLOW
            val textColor: Int =
                if (binding.common.swTextColor.isChecked) Color.WHITE else Color.GREEN
            val popupBackgroundColor: Int =
                if (binding.common.swPopup.isChecked) Color.parseColor("#EFEFEE") else Color.parseColor(
                    "#EDE7F6"
                )
            val buttonColor: Int =
                if (binding.common.swButton.isChecked) Color.parseColor("#EE0033") else Color.YELLOW
            val frontFace =
                if (binding.face.swFrontFace.isChecked) EkycConfig.CameraMode.FRONT else EkycConfig.CameraMode.BACK
            val backCard =
                if (binding.card.swFrontCard.isChecked) EkycConfig.CameraMode.BACK else EkycConfig.CameraMode.FRONT
            val zoomLevel = binding.common.edtZoomLevel.text.toString()
                .toIntOrNull() ?: 1
            val isSmallButtonRadius =
                if (binding.common.swButtonRadius.isChecked) R.dimen.test_button_radius else com.fast.ekyc.R.dimen.kyc_radius_extra_large
            val regularFont =
                if (binding.common.swFont.isChecked) R.font.amita else com.fast.ekyc.R.font.sarabun_re
            val boldFont =
                if (binding.common.swFont.isChecked) R.font.amita_bold else com.fast.ekyc.R.font.sarabun_semibold
            val faceBottomPercentage =
                binding.common.edtFaceBottomPercentage.text.toString().toFloatOrNull() ?: 0.15f
            val idCardBoxPercentage =
                binding.common.edtIdCardBox.text.toString().toFloatOrNull() ?: 0.025f


            val flash = binding.common.swFlash.isChecked
            val showHelp = binding.common.enableShowHelp.isChecked
            val showAutoCapture = binding.common.enableAutoCapture.isChecked
            val autoCaptureMode = binding.common.swAutoCaptureMode.isChecked
            val confirm = binding.common.swConfirm.isChecked
            val cacheImage = binding.common.swCacheImage.isChecked
            val isDebug = binding.common.swIsDebug.isChecked
            val iouThreshold =
                binding.common.edtIOUThreshold.text.toString().toFloatOrNull() ?: 0.92f
            val iouCaptureTime =
                binding.common.edtIOUCaptureTime.text.toString().toIntOrNull() ?: 1000

            val fakeRetake = binding.face.edtFaceRetake.text.toString()
                .toIntOrNull() ?: 10
            val faceMin = binding.face.edtFaceMin.text.toString()
                .toFloatOrNull() ?: 0.25f
            val faceMax = binding.face.edtFaceMax.text.toString()
                .toFloatOrNull() ?: 0.7f

            val cardRetake = binding.card.edtCardRetake.text.toString()
                .toIntOrNull() ?: 10
            val cardMin = binding.card.edtCardMin.text.toString()
                .toFloatOrNull() ?: 0.6f
            val idCardABBR = binding.card.swIDCardABBR.isChecked

            val isRestricted = if (binding.advanced.switchAdvancedRestricted.isChecked) {
                EkycConfig.FaceAdvancedMode.RESTRICTED
            } else {
                EkycConfig.FaceAdvancedMode.UNRESTRICTED
            }
            savePersistData(UI_FLOW_KEY, currentUIFlowType.name)

            val config = EkycConfigBuilder()
                .setIdCardTypes(currentTypeList.toList())
                .setUiFlowType(currentUIFlowType)
                .isCacheImage(cacheImage)
                .setFaceAdvancedMode(isRestricted)
                .setAdvancedLivenessConfig(advancedLivenessConfig)
                .setShowHelp(showHelp)
                .setShowAutoCaptureButton(showAutoCapture)
                .setAutoCaptureMode(autoCaptureMode)
                .setBackgroundColor(backgroundColor)
                .setPopupBackgroundColor(popupBackgroundColor)
                .setTextColor(textColor)
                .setButtonColor(buttonColor)
                .setButtonCornerRadius(isSmallButtonRadius)
                .setFonts(regularFont, boldFont)
                .setShowFlashButton(flash)
                .setZoom(zoomLevel)
                .setIdCardCameraMode(backCard)
                .setSelfieCameraMode(frontFace)
                .setDebug(isDebug)
                .setIouCaptureTime(iouCaptureTime)
                .setIouThreshold(iouThreshold)
                .setSkipConfirmScreen(confirm)
                .setFaceMinRatio(faceMin)
                .setFaceMaxRatio(faceMax)
                .setFaceRetakeLimit(fakeRetake)
                .setIdCardMinRatio(cardMin)
                .setIdCardRetakeLimit(cardRetake)
                .setIdCardAbbr(idCardABBR)
                .isCacheImage(cacheImage)
                .setIdCardBoxPercentage(idCardBoxPercentage)
                .setFaceBottomPercentage(faceBottomPercentage)
                .build()

            FastEkycSDK.startEkyc(
                activity = this,
                config = config
            )
        } catch (error: Exception) {
            showToast(error.message ?: "Unknown Error")
        }

        setupTracking()
    }

    private val dateFormat = SimpleDateFormat("yyyy/MM/dd HH/mm/ss", Locale.getDefault())
    private val tracking = object : EkycTracking {
        override fun createEventAndTrack(
            objectName: String,
            eventSrc: EventSrc,
            objectType: ObjectType,
            action: EventAction,
            eventValue: Map<String, Any>?,
        ) {
            trackingContent += "---------------------- \n" +
                    "${dateFormat.format(Date())} \n" +
                    "Event Name: $objectName \n\n" +
                    "Event Source: ${eventSrc.name} \n\n" +
                    "Object Type: ${objectType.name} \n\n" +
                    "Action: ${action.name} \n\n" +
                    "Event Value: ${eventValue.toString()} \n\n" +
                    "\n\n\n"
        }
    }

    private fun setupTracking() {
        trackingContent = ""
        FastEkycSDK.setTracker(tracking)
    }


    private fun savePersistData(
        key: String,
        value: String,
    ) {
        getDefaultSharedPreferences(this)
            .edit().putString(key, value).apply()
    }

    private fun getPersistData(
        key: String,
    ): String {
        return getDefaultSharedPreferences(this).getString(key, "") ?: ""
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val result = FastEkycSDK.getUiFlowResult(requestCode, resultCode, data)
        result?.let {
            if (supportFragmentManager.findFragmentByTag(ResultUIBottomSheet.TAG) == null) {
                ResultUIBottomSheet.newInstance(result)
                    .show(supportFragmentManager, ResultUIBottomSheet.TAG)
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    companion object {
        const val SHARED_ID_CARD_KEY = "card"
        const val SHARED_BIRTH_KEY = "birth"
        const val SHARED_EXPIRED_KEY = "expired"
        const val UI_FLOW_KEY = "ui_flow"
    }
}

