package com.fast.ekyc.data.network.interceptor

import com.fast.ekyc.utils.DataHolder
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import javax.inject.Inject

internal class KycInterceptor @Inject constructor(
    private val token: String,
    private val requestId: String?,
    private val isDebug: Boolean
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val newRequest = newRequestWithAccessToken(request)

        if (isDebug) {
            DataHolder.addRequest(newRequest.toString())
        }

        return chain.proceed(newRequest)
    }

    private fun newRequestWithAccessToken(
        request: Request,
    ): Request {
        val builder =  request.newBuilder().header(HEADER_AUTHORIZATION, token)

        if (!requestId.isNullOrBlank()){
            builder.header(REQUEST_ID, requestId)
        }

        return builder.build()
    }

    companion object {
        private const val HEADER_AUTHORIZATION = "token"
        private const val REQUEST_ID = "x_request_id"
    }
}