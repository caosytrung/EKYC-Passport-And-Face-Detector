package com.fast.ekyc.ui.card.card_guide_popup

import android.graphics.Color
import com.fast.ekyc.R
import com.fast.ekyc.base.ui.ApolloBottomSheetSize
import com.fast.ekyc.base.ui.BaseBottomSheet
import com.fast.ekyc.databinding.KycBottomSheetCardMoveToBackBinding
import com.fast.ekyc.theme.ThemeHolder

internal class CardMoveToBackBottomSheet : BaseBottomSheet<KycBottomSheetCardMoveToBackBinding>() {

    private var onMove: (() -> Unit)? = null
    override fun initComponents() {
        viewDataBinding.apply {
            btnUnderstand.setOnClickListener {
                onMove?.invoke()
                dismiss()
            }
            btnUnderstand.setButtonTextColor(Color.WHITE)
            btnUnderstand.setButtonBackgroundColor(
                ThemeHolder.buttonColor
            )
        }

        isCancelable = false
    }

    override fun getLayoutId() = R.layout.kyc_bottom_sheet_card_move_to_back

    override fun clearComponents() {
        onMove = null
    }

    override fun getBottomSheetSize(): ApolloBottomSheetSize {
        return ApolloBottomSheetSize.FULL_SCREEN
    }

    companion object {
        const val TAG = "CardMoveToBackBottomSheet"

        fun getInstance( onMove: (() -> Unit)? = null): CardMoveToBackBottomSheet {
            return CardMoveToBackBottomSheet().apply {
                this.onMove = onMove
            }
        }
    }
}