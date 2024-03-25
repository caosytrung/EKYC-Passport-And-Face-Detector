package com.fast.ekyc.ui.result_popup

import android.graphics.Color
import com.fast.ekyc.R
import com.fast.ekyc.base.ui.BaseBottomSheet
import com.fast.ekyc.databinding.KycBottomSheetImageValidBinding
import com.fast.ekyc.theme.ThemeHolder

internal class ImageValidBottomSheet : BaseBottomSheet<KycBottomSheetImageValidBinding>() {

    private var onRecapture: (() -> Unit)? = null
    private var useImage: (() -> Unit)? = null

    override fun initComponents() {
        isCancelable = false
        viewDataBinding.btnRecapture.setOnClickListener {
            dismiss()
            onRecapture?.invoke()
        }

        viewDataBinding.btnUseImage.apply {
            setButtonTextColor(Color.WHITE)
            setButtonBackgroundColor(ThemeHolder.buttonColor)
            setOnClickListener {
                dismiss()
                useImage?.invoke()
            }
        }

    }

    override fun getLayoutId() = R.layout.kyc_bottom_sheet_image_valid

    override fun clearComponents() {
        onRecapture = null
        useImage = null
    }

    companion object {
        const val TAG = "ImageValidBottomSheet"

        fun newInstance(
            onRecapture: (() -> Unit)? = null,
            useImage: (() -> Unit)? = null,
        ) =
            ImageValidBottomSheet().apply {
                this.onRecapture = onRecapture
                this.useImage = useImage
            }
    }
}