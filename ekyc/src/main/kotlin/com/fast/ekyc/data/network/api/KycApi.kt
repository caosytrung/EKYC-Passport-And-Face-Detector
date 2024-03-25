package com.fast.ekyc.data.network.api

import com.google.gson.GsonBuilder
import com.fast.ekyc.utils.data.GsonObjectNormalizer

internal interface KycApi {

    companion object {
        internal val DEFAULT_GSON = GsonBuilder()
            .registerTypeAdapterFactory(GsonObjectNormalizer())
            .excludeFieldsWithoutExposeAnnotation()
            .create()
    }
}
