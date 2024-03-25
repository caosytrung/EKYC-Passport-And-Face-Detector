package com.fast.ekyc.base.ui.camera

internal class CustomSize(var width: Int, var height: Int) {
    fun getRatio() = width.toFloat() / height

    fun scaleSmaller(ratio: Float) {
        width = (width * ratio).toInt()
        height = (height * ratio).toInt()
    }
}