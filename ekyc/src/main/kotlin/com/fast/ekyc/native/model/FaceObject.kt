package com.fast.ekyc.native.model

data class FaceObject(
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val confidence: Float,
)