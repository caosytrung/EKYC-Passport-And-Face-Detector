package com.fast.ekyc.utils.extension

import android.content.Context
import com.fast.ekyc.R
import com.fast.ekyc.data.config.request.EkycConfig
import com.fast.ekyc.ui.face.face_capture.AdvancedFaceState

internal fun EkycConfig.shouldOpenFaceIdentification(): Boolean {
    return uiFlowType.isFace()
}

internal fun EkycConfig.buildDisplayCardType(context: Context): String {
    if (idCardTypes.size == 1) return getCardName(idCardTypes.first(), context, idCardAbbr)

    var result = ""

    idCardTypes.forEachIndexed { index, idType ->
        result += getCardName(idType, context, idCardAbbr)
        if (index < idCardTypes.size - 1) {
            result += "/"
        }
    }

    return result
}

internal fun getCardName(type: EkycConfig.IdCardType, context: Context, isAbbr: Boolean): String {
    return when (type) {
        EkycConfig.IdCardType.CMND -> {
            context.getString(if (isAbbr) R.string.kyc_card_display_abbr_CMND else R.string.kyc_card_display_CMND)
        }
        EkycConfig.IdCardType.CCCD -> {
            context.getString(if (isAbbr) R.string.kyc_card_display_abbr_CCCD else R.string.kyc_card_display_CCCD)
        }
        EkycConfig.IdCardType.PASSPORT -> context.getString(R.string.kyc_card_display_HC)
        EkycConfig.IdCardType.CMQD -> context.getString(if (isAbbr) R.string.kyc_card_display_abbr_CMQD else R.string.kyc_card_display_CMQD)
        EkycConfig.IdCardType.BLX -> context.getString(if (isAbbr) R.string.kyc_card_display_abbr_BLX else R.string.kyc_card_display_BLX)
    }
}

internal fun AdvancedFaceState.getDisplayIcon(): Int {
    return when (this) {
        AdvancedFaceState.LEFT -> R.drawable.kyc_ic_face_left
        AdvancedFaceState.RIGHT -> R.drawable.kyc_ic_face_right

        AdvancedFaceState.PORTRAIT -> -1
    }
}

internal fun AdvancedFaceState.getDisplayText(): Int {
    return when (this) {
        AdvancedFaceState.LEFT -> R.string.kyc_ic_face_left
        AdvancedFaceState.RIGHT -> R.string.kyc_ic_face_right
        else -> -1
    }
}

internal fun EkycConfig.isPassportOnly() =
    this.idCardTypes.size == 1 && this.idCardTypes.first() == EkycConfig.IdCardType.PASSPORT

internal fun EkycConfig.UiFlowType.isFace() =
    this == EkycConfig.UiFlowType.FACE_ADVANCED || this == EkycConfig.UiFlowType.FACE_BASIC

internal fun EkycConfig.UiFlowType.isFront() = this == EkycConfig.UiFlowType.ID_CARD_FRONT

internal fun EkycConfig.isAdvancedMode(): Boolean {
    return uiFlowType == EkycConfig.UiFlowType.FACE_ADVANCED
}

internal fun List<EkycConfig.IdCardType>.isFrontCardOnly(): Boolean {
    return any { it == EkycConfig.IdCardType.BLX || it == EkycConfig.IdCardType.PASSPORT }
}