package com.fast.ekyc.data.network.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class ErrorResponse(
    @SerializedName("code")
    @Expose
    val code: Int,

    @SerializedName("message")
    @Expose
    val message: String,
)