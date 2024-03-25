package com.fast.ekyc.utils

import android.app.Activity
import android.view.Window
import android.view.WindowManager

object BrightnessManager {
    private var lastScreenBrightness: Float? = null

    fun turnOnFlash(activity: Activity) {
        val window: Window = activity.window
        val params: WindowManager.LayoutParams = window.attributes

        if (lastScreenBrightness == null) {
            lastScreenBrightness = params.screenBrightness
        }
        params.screenBrightness = 1f
        window.attributes = params

//        val hsv = FloatArray(3)
//        Color.colorToHSV(Color.WHITE, hsv)
//        hsv[2] *= 1f
//        val color = Color.HSVToColor(hsv)
    }

    fun turnOffFlash(activity: Activity) {
        lastScreenBrightness?.let {
            val windowParam = activity.window.attributes
            windowParam.screenBrightness = it
            activity.window.attributes = windowParam
        }
    }

    fun makeBrightnessTo80Percent(activity: Activity) {
        activity.window?.also {
            val layout = it.attributes ?: return
            layout.screenBrightness = 0.8f
            it.attributes = layout
        }
    }
}