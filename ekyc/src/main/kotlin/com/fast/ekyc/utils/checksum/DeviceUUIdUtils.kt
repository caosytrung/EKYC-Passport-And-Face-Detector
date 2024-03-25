package com.fast.ekyc.utils.checksum

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Build
import android.provider.Settings
import java.util.*

internal object DeviceUUIdUtils {
    var deviceUUID: String = ""
        private set

    @SuppressLint("HardwareIds")
    fun configUuidIfNeeded(context: Activity) {
        if (deviceUUID.isEmpty()) {
            deviceUUID = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ANDROID_ID
            )
        }
    }

    fun generateChecksum(): String {
        val checksumUtils = ChecksumUtils()
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            checksumUtils.generateChecksum(deviceUUID)
        } else {
            UUID.randomUUID().toString()
        }
    }
}