package com.fast.ekyc.native

import android.content.res.AssetManager
import android.graphics.Bitmap
import com.fast.ekyc.native.model.CardObject

internal object NativeFunctionCall {
    init {
        System.loadLibrary("cpp1")
    }

    external fun init(mgr: AssetManager, configPath: String)

    external fun detectPose(bitmap: Bitmap): FloatArray

    external fun detectCard(bitmap: Bitmap): Array<CardObject>
}