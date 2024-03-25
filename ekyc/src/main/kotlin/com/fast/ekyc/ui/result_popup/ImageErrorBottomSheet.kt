package com.fast.ekyc.ui.result_popup

import androidx.core.os.bundleOf
import androidx.core.view.isGone
import com.fast.ekyc.R
import com.fast.ekyc.base.ui.BaseBottomSheet
import com.fast.ekyc.databinding.KycBottomSheetImageErrorBinding
import com.fast.ekyc.theme.ThemeHolder
import com.fast.ekyc.utils.ShapeBuilder

internal class ImageErrorBottomSheet : BaseBottomSheet<KycBottomSheetImageErrorBinding>() {

    private var onClose: (() -> Unit)? = null

    override fun initComponents() {
        isCancelable = false
        viewDataBinding.apply {
            btnRecapture.setOnClickListener {
                dismiss()
                onClose?.invoke()
            }

            val title = arguments?.getString(TITLE_KEY) ?: ""
            val content = arguments?.getString(CONTENT_KEY) ?: ""
            val close = arguments?.getString(CLOSE_KEY) ?: ""
            val closeButtonVisible = arguments?.getBoolean(IS_CLOSE_BUTTON_VISIBLE_KEY) ?: true

            btnRecapture.isGone = !closeButtonVisible

//        (if (isRecord) R.string.kyc_rerecord else R.string.kyc_recapture)

            btnRecapture.text = close
            tvTitle.text = title
            if (content.isNotEmpty()) {
                tvContent.text = content
            } else {
                tvContent.isGone = true
            }

            val context = context ?: return
            val background = ShapeBuilder(context)
                .withRadius(R.dimen.kyc_radius_extra)
                .withColor(ThemeHolder.popupBackgroundColor)
                .build()

            lnContent.background = background
        }
    }

    override fun getLayoutId() = R.layout.kyc_bottom_sheet_image_error

    override fun clearComponents() {
        onClose = null
    }

    companion object {
        const val TAG = "ImageErrorBottomSheet"
        private const val TITLE_KEY = "title"
        private const val CONTENT_KEY = "content"
        private const val CLOSE_KEY = "close"
        private const val IS_CLOSE_BUTTON_VISIBLE_KEY = "IS_CLOSE_BUTTON_VISIBLE"

        fun newInstance(
            title: String,
            content: String,
            closeButton: String,
            isCloseButtonVisible: Boolean = true,
            onClose: (() -> Unit)? = null
        ) =
            ImageErrorBottomSheet().apply {
                val bundle = bundleOf(
                    Pair(TITLE_KEY, title),
                    Pair(CONTENT_KEY, content),
                    Pair(CLOSE_KEY, closeButton),
                    Pair(IS_CLOSE_BUTTON_VISIBLE_KEY, isCloseButtonVisible),
                )
                arguments = bundle
                this.onClose = onClose
            }
    }
}