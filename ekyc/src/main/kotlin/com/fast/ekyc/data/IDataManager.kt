package com.fast.ekyc.data

import com.fast.ekyc.utils.Result

internal interface IDataManager {
    suspend fun validateSomethingRemotely(): Result<Any, Throwable>
}