package com.fast.ekyc.data

import com.fast.ekyc.data.config.request.EkycConfig
import com.fast.ekyc.data.network.api.KycApi
import com.fast.ekyc.utils.Result
import com.fast.ekyc.utils.apiResult
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class DataManager @Inject constructor(
    private val config: EkycConfig,
    private val api: KycApi
) : IDataManager {
    override suspend fun validateSomethingRemotely(): Result<Any, Throwable> = apiResult {
        TODO("Not yet implemented")
    }
}