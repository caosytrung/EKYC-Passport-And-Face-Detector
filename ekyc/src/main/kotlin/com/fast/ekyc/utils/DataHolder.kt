package com.fast.ekyc.utils

import android.graphics.Bitmap
import com.fast.ekyc.data.model.AdvanceImageData

internal object DataHolder {
    private var cardBitmap: Bitmap? = null
    private var faceBitmap: Bitmap? = null

    var uiFlowLocalFullImage: Bitmap? = null
    var localFrontCardFullImage: Bitmap? = null
    var localBackCardFullImage: Bitmap? = null
    var localFaceFullImage: Bitmap? = null

    var uiFlowLocalUploadImage: Bitmap? = null
    var localFrontCardUploadImage: Bitmap? = null
    var localBackCardUploadImage: Bitmap? = null
    var localFaceUploadImage: Bitmap? = null

    var uiOnlyAdvanceImageDataList: List<AdvanceImageData>? = null

    var requests = mutableListOf<String>()

    fun addRequest(request: String) {
        requests.add(request)
        requests.add("\n----------------\n")
    }

    fun setCardBitmap(bitmap: Bitmap) {
        cardBitmap = bitmap
    }

    fun setFaceBitmap(bitmap: Bitmap) {
        faceBitmap = bitmap
    }

    fun getCardBitmap() = cardBitmap
    fun getFaceBitmap() = faceBitmap
    fun clear() {
        cardBitmap = null
        faceBitmap = null
        uiFlowLocalFullImage = null
        localFrontCardFullImage = null
        localBackCardFullImage = null
        localFaceFullImage = null
        uiFlowLocalUploadImage = null
        localFrontCardUploadImage = null
        localBackCardUploadImage = null
        localFaceUploadImage = null

        uiOnlyAdvanceImageDataList = null

        requests.clear()
    }
}