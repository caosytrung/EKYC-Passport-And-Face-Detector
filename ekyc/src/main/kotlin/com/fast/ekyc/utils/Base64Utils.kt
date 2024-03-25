package com.fast.ekyc.utils

import android.util.Base64


internal object Base64Utils {
    fun byteArrayToBase64(byteData: ByteArray): String? {
        return Base64.encodeToString(byteData, Base64.NO_WRAP)
    }

    fun stringToBase(input: String): String {
        val data: ByteArray = input.toByteArray(Charsets.UTF_8)
        return Base64.encodeToString(data, Base64.NO_WRAP)
    }
}