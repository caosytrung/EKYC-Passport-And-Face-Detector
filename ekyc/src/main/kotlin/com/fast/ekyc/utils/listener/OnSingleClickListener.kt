package com.fast.ekyc.utils.listener

import android.os.SystemClock
import android.view.View
import android.view.View.OnClickListener

internal class OnSingleClickListener(
    private val onClickWithInterval: OnClickListener,
    private val clickInterval: Int = DEFAULT_CLICK_INTERVAL
) : OnClickListener {
    private var lastClickTime: Long = 0

    override fun onClick(v: View) {
        val currentClickTime = SystemClock.uptimeMillis()
        val elapsedTime = currentClickTime - lastClickTime

        if (elapsedTime > clickInterval) {
            onClickWithInterval.onClick(v)
            lastClickTime = currentClickTime
        }
    }

    companion object {
        const val DEFAULT_CLICK_INTERVAL: Int = 500
    }
}