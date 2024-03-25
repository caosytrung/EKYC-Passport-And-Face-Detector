package com.fast.ekyc.data.model

enum class ResultState {
    Success,
    UserCancelled;

    fun isCancelled() = this == UserCancelled
}