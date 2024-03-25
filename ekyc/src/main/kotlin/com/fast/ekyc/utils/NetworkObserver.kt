package com.fast.ekyc.utils

internal interface NetworkObserver {
    fun onConnectivityChange(isOnline: Boolean)
}