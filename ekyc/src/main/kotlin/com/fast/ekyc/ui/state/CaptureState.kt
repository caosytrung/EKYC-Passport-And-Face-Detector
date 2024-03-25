package com.fast.ekyc.ui.state

internal enum class CaptureState {
    VALID,
    TOO_BIG,
    TOO_SMALL,
    UNKNOWN,
    NO_PORTRAIT,
    LIMITED,
    MOVE_OUT_OF_FRAME,
    WRONG_TYPE,
    KEEP_STRAIGHT,
    NOTICE,
    TOO_MANY_OBJECT;

    fun isCardInsideFrame() = this == VALID

    fun isUnknown() = this == UNKNOWN

    fun isTooBig() = this == TOO_BIG

    fun isTooSmall() = this == TOO_SMALL

    fun isNotPortrait() = this == NO_PORTRAIT

    fun isValid() = this == VALID

    fun isTooManyObject() = this == TOO_MANY_OBJECT

    fun isLimited() = this == LIMITED

    fun isMoveOut() = this == MOVE_OUT_OF_FRAME

    fun isWrongType() = this == WRONG_TYPE

    fun isKeepStraight() = this == KEEP_STRAIGHT

    fun isNotice() = this == NOTICE
}