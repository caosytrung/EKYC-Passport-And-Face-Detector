package com.fast.ekyc.native.model

import com.fast.ekyc.data.config.request.EkycConfig

class CardObject(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float,
    val confidence: Float,
    val className: String,
) {
    enum class AICardType {
        BLX,
        BLX_BACK,
        BLX_BACK_OLD,
        BLX_OLD,
        CCCD,
        CCCD_BACK,
        CCCD_back_chip,
        CCCD_front_chip,
        CMCC,
        CMND,
        CMND_BACK,
        CMQD,
        CMQD_BACK,
        PASSPORT,
        PASSPORT_OTHER;

        fun isCCCDChip() = this == CCCD_back_chip || this == CCCD_front_chip
    }

    fun getWidth() = right - left
    fun getHeight() = bottom - top

    fun toIdCardTypeAndSide(): CardTypeAndSide {
        return when (className) {
            "BLX" -> CardTypeAndSide(EkycConfig.IdCardType.BLX, aiCardType = AICardType.BLX)
            "BLX_BACK" -> CardTypeAndSide(
                EkycConfig.IdCardType.BLX,
                false,
                aiCardType = AICardType.BLX_BACK
            )
            "BLX_BACK_OLD" -> CardTypeAndSide(
                EkycConfig.IdCardType.BLX,
                false,
                aiCardType = AICardType.BLX_BACK_OLD
            )
            "BLX_OLD" -> CardTypeAndSide(EkycConfig.IdCardType.BLX, aiCardType = AICardType.BLX_OLD)
            "CCCD" -> CardTypeAndSide(EkycConfig.IdCardType.CCCD, aiCardType = AICardType.CCCD)
            "CCCD_BACK" -> CardTypeAndSide(
                EkycConfig.IdCardType.CCCD,
                false,
                aiCardType = AICardType.CCCD_BACK
            )
            "CCCD_back_chip" -> CardTypeAndSide(
                EkycConfig.IdCardType.CCCD,
                isFront = false,
                aiCardType = AICardType.CCCD_back_chip
            )
            "CCCD_front_chip" -> CardTypeAndSide(
                EkycConfig.IdCardType.CCCD,
                aiCardType = AICardType.CCCD_front_chip
            )
            "CMCC" -> CardTypeAndSide(EkycConfig.IdCardType.CMND, aiCardType = AICardType.CMCC)
            "CMND" -> CardTypeAndSide(EkycConfig.IdCardType.CMND, aiCardType = AICardType.CMND)
            "CMND_BACK" -> CardTypeAndSide(
                EkycConfig.IdCardType.CMND,
                false,
                aiCardType = AICardType.CMND_BACK
            )
            "CMQD" -> CardTypeAndSide(EkycConfig.IdCardType.CMQD, aiCardType = AICardType.CMQD)
            "CMQD_BACK" -> CardTypeAndSide(
                EkycConfig.IdCardType.CMQD,
                false,
                aiCardType = AICardType.CMQD_BACK
            )
            "PASSPORT" -> CardTypeAndSide(
                EkycConfig.IdCardType.PASSPORT,
                aiCardType = AICardType.PASSPORT
            )
            "PASSPORT_OTHER" -> CardTypeAndSide(
                EkycConfig.IdCardType.PASSPORT,
                aiCardType = AICardType.PASSPORT_OTHER
            )
            else -> CardTypeAndSide(EkycConfig.IdCardType.CMND, aiCardType = AICardType.CMND)
        }
    }

    fun isShouldLowConfidence() = className == "CMCC"
}

data class CardTypeAndSide(
    val cardType: EkycConfig.IdCardType,
    val isFront: Boolean = true,
    val aiCardType: CardObject.AICardType
) {
    fun isCCCDBackChip() = aiCardType == CardObject.AICardType.CCCD_back_chip
}