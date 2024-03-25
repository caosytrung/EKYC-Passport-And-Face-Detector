package com.fast.ekyc.ui.state

internal enum class VerificationState {
    INIT,
    INVALID,
    VALID;

    fun isValid() = this == VALID
    fun isInvalid() = this == INVALID
}