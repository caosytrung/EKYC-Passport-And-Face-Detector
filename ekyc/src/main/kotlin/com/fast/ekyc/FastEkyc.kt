package com.fast.ekyc

import android.app.Activity
import android.content.Intent
import androidx.fragment.app.Fragment
import com.fast.ekyc.data.config.request.EkycConfig
import com.fast.ekyc.data.model.KycUIFlowResult
import com.fast.ekyc.data.model.ResultState
import com.fast.ekyc.serviceHandler.EkycServiceHandler
import com.fast.ekyc.tracking.EkycTracking
import com.fast.ekyc.ui.main.MainActivity
import com.fast.ekyc.utils.DataHolder

object FastEkycSDK {
    internal var tracker: EkycTracking? = null

    @JvmStatic
    fun setTracker(tracker: EkycTracking) {
        this.tracker = tracker
    }


    @JvmStatic
    fun startEkyc(activity: Activity, config: EkycConfig) {
        EkycServiceHandler.startEkyc(activity, config)
    }

    @JvmStatic
    fun startEkyc(fragment: Fragment, config: EkycConfig) {
        EkycServiceHandler.startEkyc(fragment, config)
    }

    @JvmStatic
    fun getUiFlowResult(requestCode: Int, resultCode: Int, data: Intent?): KycUIFlowResult? {
        if (requestCode == EKYC_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                return data.getParcelableExtra<KycUIFlowResult?>(MainActivity.RESULT_DATA)?.also {
                    it.localFullImage = DataHolder.uiFlowLocalFullImage
                    it.localCroppedImage = DataHolder.uiFlowLocalUploadImage
                    it.advanceImageDataList = DataHolder.uiOnlyAdvanceImageDataList
                }
            }

            return KycUIFlowResult(
                resultState = ResultState.UserCancelled
            )
        }

        return null
    }

    const val EKYC_REQUEST_CODE = 10001
}
