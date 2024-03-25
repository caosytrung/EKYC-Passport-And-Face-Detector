package com.fast.ekyc.serviceHandler

import android.app.Activity
import androidx.fragment.app.Fragment
import com.fast.ekyc.FastEkycSDK
import com.fast.ekyc.data.config.request.EkycConfig
import com.fast.ekyc.ui.main.MainActivity

internal object EkycServiceHandler {

    @Throws
    fun startEkyc(activity: Activity, config: EkycConfig) {
        activity.startActivityForResult(
            MainActivity.buildIntent(activity, config),
            FastEkycSDK.EKYC_REQUEST_CODE
        )
    }

    @Throws
    fun startEkyc(fragment: Fragment, config: EkycConfig) {
        fragment.context?.also { context ->
            fragment.startActivityForResult(
                MainActivity.buildIntent(context, config),
                FastEkycSDK.EKYC_REQUEST_CODE
            )
        }
    }
}