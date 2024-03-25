package com.fast.ekyc.base.ui

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.fast.ekyc.R
import com.fast.ekyc.utils.autoCleared
import com.fast.ekyc.utils.extension.setOnSingleClickListener

abstract class BaseBottomSheet<T : ViewDataBinding> : BottomSheetDialogFragment() {
    private var bottomSheetBehavior: BottomSheetBehavior<*>? = null
    private var bottomSheetCallback: BottomSheetBehavior.BottomSheetCallback? = null
    protected var viewDataBinding by autoCleared<T>()

    override fun getTheme(): Int = R.style.KycBaseBottomSheetDialog

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewDataBinding = DataBindingUtil.inflate(inflater, getLayoutId(), container, false)
        return viewDataBinding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setOnShowListener { d ->
            bottomSheetBehavior = (d as BottomSheetDialog).behavior
            bottomSheetBehavior?.apply {
                state = BottomSheetBehavior.STATE_EXPANDED
                bottomSheetCallback = object : BottomSheetBehavior.BottomSheetCallback() {
                    override fun onSlide(
                        bottomSheet: View,
                        slideOffset: Float
                    ) {
                    }

                    override fun onStateChanged(
                        bottomSheet: View,
                        newState: Int
                    ) {
                        onStageChanged(newState)
                    }
                }
                addBottomSheetCallback(bottomSheetCallback!!)
            }
        }
        return dialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewDataBinding.lifecycleOwner = viewLifecycleOwner
        viewDataBinding.executePendingBindings()
        initComponents()
        setUpFixedHeight()
    }

    private fun setUpFixedHeight() {
        val fixedHeight = getBottomSheetSize()
        val bottomSheet: View = dialog?.findViewById(R.id.design_bottom_sheet) ?: return
        if (fixedHeight.isWrapContent()) return

        val screenHeight = resources.displayMetrics.heightPixels
        val offsetFromTop = (screenHeight * (1 - fixedHeight.percent)).toInt()
        (dialog as? BottomSheetDialog)?.behavior?.apply {
            isFitToContents = false
            expandedOffset = offsetFromTop
            bottomSheet.layoutParams.height = screenHeight - offsetFromTop

            state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    open fun getBottomSheetSize(): ApolloBottomSheetSize = ApolloBottomSheetSize.WRAP_CONTENT

    override fun setCancelable(cancelable: Boolean) {
        super.setCancelable(cancelable)

        if (!cancelable) {
            val outside =
                dialog?.window?.decorView?.findViewById<View>(
                    com.google.android.material.R.id.touch_outside
                )
            outside?.setOnSingleClickListener(listener = null)

            val bottomSheet =
                dialog?.window?.decorView?.findViewById<View>(
                    com.google.android.material.R.id.design_bottom_sheet
                )
            bottomSheet?.let {
                BottomSheetBehavior.from(it)
                    .isHideable = false
            }
        }

    }

    open fun onStageChanged(newState: Int) {
        if (newState == BottomSheetBehavior.STATE_COLLAPSED || newState == BottomSheetBehavior.STATE_HIDDEN) {
            dismiss()
        }
    }

    @LayoutRes
    abstract fun getLayoutId(): Int

    abstract fun clearComponents()

    abstract fun initComponents()

}

enum class ApolloBottomSheetSize(val percent: Float) {
    WRAP_CONTENT(-1f),
    PERCENT_40(0.4f),
    PERCENT_60(0.6f),
    PERCENT_80(0.8f),
    FULL_SCREEN(1f);

    internal fun isWrapContent() = this == WRAP_CONTENT
}