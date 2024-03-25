package com.fast.ekyc.ui.card.card_guide_popup

import android.content.DialogInterface
import androidx.core.os.bundleOf
import com.fast.ekyc.R
import com.fast.ekyc.FastEkycSDK
import com.fast.ekyc.base.ui.BaseBottomSheet
import com.fast.ekyc.databinding.KycBottomSheetCardGuideBinding
import com.fast.ekyc.theme.ThemeHolder
import com.fast.ekyc.tracking.EventAction
import com.fast.ekyc.tracking.EventName
import com.fast.ekyc.tracking.EventSrc
import com.fast.ekyc.tracking.ObjectType
import com.fast.ekyc.utils.ShapeBuilder

internal class CardGuideBottomSheet : BaseBottomSheet<KycBottomSheetCardGuideBinding>() {

    private var dismiss: (() -> Unit)? = null

    private var isHC = false

    override fun initComponents() {
        FastEkycSDK.tracker?.createEventAndTrack(
            objectName = EventName.CARD_CAPTURE_GUIDE_HC_POPUP_SHOW,
            eventSrc = EventSrc.APP,
            objectType = ObjectType.POPUP,
            action = EventAction.SHOW
        )

        viewDataBinding.btnUnderstand.setOnClickListener {
            FastEkycSDK.tracker?.createEventAndTrack(
                objectName = EventName.CARD_CAPTURE_GUIDE_HC_POPUP_CLICK_UNDERSTAND,
                eventSrc = EventSrc.USER,
                objectType = ObjectType.BUTTON,
                action = EventAction.TAP
            )
            dismiss()
        }

        val context = context ?: return
        val background = ShapeBuilder(context)
            .withRadius(R.dimen.kyc_radius_extra)
            .withColor(ThemeHolder.popupBackgroundColor)
            .build()

        viewDataBinding.lnContent.background = background

        arguments?.getBoolean(IS_PASSPORT)?.let { isHC ->
            this.isHC = isHC

            if (isHC) {

                viewDataBinding.ivSample.setImageResource(R.drawable.kyc_sample_card_id_hc)
                viewDataBinding.ivGuide1.setImageResource(R.drawable.kyc_card_popup_guide1_hc)
                viewDataBinding.ivGuide2.setImageResource(R.drawable.kyc_card_popup_guide2_hc)
                viewDataBinding.ivGuide3.setImageResource(R.drawable.kyc_card_popup_guide3_hc)
                viewDataBinding.ivGuide4.setImageResource(R.drawable.kyc_card_popup_guide4_hc)
            } else {
                viewDataBinding.ivSample.setImageResource(R.drawable.kyc_sample_card_id)
                viewDataBinding.ivGuide1.setImageResource(R.drawable.kyc_card_popup_guide1)
                viewDataBinding.ivGuide2.setImageResource(R.drawable.kyc_card_popup_guide2)
                viewDataBinding.ivGuide3.setImageResource(R.drawable.kyc_card_popup_guide3)
                viewDataBinding.ivGuide4.setImageResource(R.drawable.kyc_card_popup_guide4)
            }
        }
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)

        dismiss?.invoke()
        dismiss = null
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)

        dismiss?.invoke()
        dismiss = null
    }

    override fun getLayoutId() = R.layout.kyc_bottom_sheet_card_guide

    override fun clearComponents() {}

    companion object {
        const val TAG = "CardGuideBottomSheet"
        const val IS_PASSPORT = "IS_PASSPORT"

        fun getInstance(isPassport: Boolean,  onDismiss: (() -> Unit)? = null): CardGuideBottomSheet {
            return CardGuideBottomSheet().apply {
                val bundle = bundleOf(
                    Pair(IS_PASSPORT, isPassport),
                )
                arguments = bundle
                this.dismiss = onDismiss
            }
        }
    }
}