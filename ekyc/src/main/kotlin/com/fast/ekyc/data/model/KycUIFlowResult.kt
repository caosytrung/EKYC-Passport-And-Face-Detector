package com.fast.ekyc.data.model

import android.graphics.Bitmap
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class KycUIFlowResult(
    override val resultState: ResultState,
    val imageId: String = "",
    var localCroppedImage: Bitmap? = null,
    var localFullImage: Bitmap? = null,
    var advanceImageDataList: List<AdvanceImageData>? = null,
) : KycResult(resultState), Parcelable

@Parcelize
class AdvanceImageData(
    val imageId: String? = null,
    val localImage: Bitmap? = null,
    val labelPose: String? = null,
) : Parcelable
