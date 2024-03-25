package com.fast.ekyc.data.model


abstract class KycResult(
    open val resultState: ResultState,
) {
    enum class State {
        Success,
        UserCancelled,
        Error
    }
}