package com.fast.ekyc.ui.face.normal_face_guide

import android.content.DialogInterface
import androidx.core.os.bundleOf
import androidx.core.view.isGone
import com.fast.ekyc.R
import com.fast.ekyc.FastEkycSDK
import com.fast.ekyc.base.ui.BaseBottomSheet
import com.fast.ekyc.databinding.KycBottomSheetFaceGuideBinding
import com.fast.ekyc.theme.ThemeHolder
import com.fast.ekyc.tracking.EventAction
import com.fast.ekyc.tracking.EventName
import com.fast.ekyc.tracking.EventSrc
import com.fast.ekyc.tracking.ObjectType
import com.fast.ekyc.utils.ShapeBuilder

internal class FaceGuideBottomSheet : BaseBottomSheet<KycBottomSheetFaceGuideBinding>() {
    private var dismiss: (() -> Unit)? = null

    override fun initComponents() {
        FastEkycSDK.tracker?.createEventAndTrack(
            objectName = EventName.FACE_CAPTURE_GUIDE_POPUP_SHOW,
            eventSrc = EventSrc.APP,
            objectType = ObjectType.POPUP,
            action = EventAction.SHOW
        )

        viewDataBinding.btnUnderstand.setOnClickListener {
            FastEkycSDK.tracker?.createEventAndTrack(
                objectName = EventName.FACE_CAPTURE_GUIDE_POPUP_CLICK_CONFIRM,
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

        viewDataBinding.lnFaceGuide.background = background

        arguments?.getBoolean(IS_ADVANCED)?.also { isAdvanced ->
            viewDataBinding.lnFacePortrait.isGone = isAdvanced

            val titleRes = if (isAdvanced) {
                R.string.kyc_face_advanced_notice_title
            } else {
                R.string.kyc_face_capture_notice_title
            }
            viewDataBinding.tvTitle.setText(titleRes)
        }
    }

    override fun getLayoutId() = R.layout.kyc_bottom_sheet_face_guide

    override fun clearComponents() {}

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

    companion object {
        const val TAG = "FaceGuideBottomSheet"
        private const val IS_ADVANCED = "IS_ADVANCED"

        fun getInstance(isAdvanced: Boolean, onDismiss: (() -> Unit)? = null): FaceGuideBottomSheet {
            return FaceGuideBottomSheet().apply {
                val bundle = bundleOf(
                    Pair(IS_ADVANCED, isAdvanced),
                )
                arguments = bundle
                this.dismiss = onDismiss
            }
        }
    }
}