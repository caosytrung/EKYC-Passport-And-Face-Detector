package com.fast.ekyc.base.ui

import com.fast.ekyc.utils.Result

internal sealed class ViewState {

    object IDLE : ViewState()

    object LOADING : ViewState()

    object SUCCESS : ViewState()

    open class ERROR(val error: Throwable? = null) : ViewState()

    fun isLoading() = this is LOADING

    fun isSuccess() = this is SUCCESS

    fun isError() = this is ERROR

}

internal fun <V : Any, E : Throwable> Result<V, E>.toViewState(): ViewState = when {
    isSuccess() -> ViewState.SUCCESS
    else -> ViewState.ERROR(exception())
}