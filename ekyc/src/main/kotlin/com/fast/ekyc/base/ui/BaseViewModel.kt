package com.fast.ekyc.base.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.fast.ekyc.exception.KycApiException

internal abstract class BaseViewModel : ViewModel() {

    private var state = MutableLiveData<ViewState>(ViewState.IDLE)

    fun getState(): LiveData<ViewState> = state

    fun setState(state: ViewState) {
        this.state.postValue(state)
    }

    open fun showLoading(){
        setState(ViewState.LOADING)
    }

    open fun hideLoading(){
        setState(ViewState.SUCCESS)
    }

    fun getMessageFromException(throwable: Throwable): String{
        if (throwable is KycApiException){
            return throwable.serverMessage
        }

        return "Có lỗi xảy ra"
    }
}
