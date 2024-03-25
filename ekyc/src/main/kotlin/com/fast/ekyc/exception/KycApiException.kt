package com.fast.ekyc.exception

internal class KycApiException(
    val code: Int,
    val serverMessage: String
) : Exception(serverMessage)
