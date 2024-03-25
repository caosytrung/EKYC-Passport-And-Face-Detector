package com.fast.ekyc.utils

import android.app.Activity
import android.content.Context
import android.hardware.Camera
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Build
import android.util.SparseIntArray
import android.view.Surface

@Suppress("DEPRECATION")
internal object CameraHelper {
    private val orientations = SparseIntArray()

    init {
        orientations.append(Surface.ROTATION_0, 0)
        orientations.append(Surface.ROTATION_90, 90)
        orientations.append(Surface.ROTATION_180, 180)
        orientations.append(Surface.ROTATION_270, 270)
    }

    /**
     * Get the angle by which an image must be rotated given the device's current
     * orientation.
     */
    fun getRotationCompensation(cameraId: Int, activity: Activity): Int {
        val isFrontFacing: Boolean = cameraId == Camera.CameraInfo.CAMERA_FACING_FRONT
        // Get the device's current rotation relative to its "native" orientation.
        // Then, from the ORIENTATIONS table, look up the angle the image must be
        // rotated to compensate for the device's rotation.
        val deviceRotation = activity.windowManager.defaultDisplay.rotation
        var rotationCompensation = orientations.get(deviceRotation)

        // Get the device's sensor orientation.

        val sensorOrientation: Int = if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            val cameraManager = activity.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            cameraManager
                .getCameraCharacteristics(cameraId.toString())
                .get(CameraCharacteristics.SENSOR_ORIENTATION)!!
        } else {
            if (isFrontFacing) {
                270
            } else {
                90
            }
        }

        rotationCompensation = if (isFrontFacing) {
            (sensorOrientation + rotationCompensation) % 360
        } else { // back-facing
            (sensorOrientation - rotationCompensation + 360) % 360
        }
        return rotationCompensation
    }
}