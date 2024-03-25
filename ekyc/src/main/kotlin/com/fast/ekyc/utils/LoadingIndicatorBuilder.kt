package com.fast.ekyc.utils

import android.content.Context
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isGone
import com.fast.ekyc.R
import com.fast.ekyc.databinding.KycLayoutLoadingIndicatorBinding

internal class LoadingIndicatorBuilder(
    val context: Context
) {
    private val viewBinding =
        KycLayoutLoadingIndicatorBinding.inflate(LayoutInflater.from(context))
    private var cancelable = false
    private var canceledOnTouchOutside = false

    fun withCancelable(cancelable: Boolean) = this.apply {
        this.cancelable = cancelable
    }


    fun withCanceledOnTouchOutside(canceledOnTouchOutside: Boolean) = this.apply {
        this.canceledOnTouchOutside = canceledOnTouchOutside
    }

    fun withLoadingContent(value: String?) =
        this.apply {
            if (value.isNullOrEmpty()) {
                viewBinding.tvLoadingContent.isGone = true
            } else {
                viewBinding.tvLoadingContent.text = value
            }
        }

    fun build(): AlertDialog {
        val alertDialog = AlertDialog.Builder(context, R.style.KycLoadingIndicatorStyle)
            .setCancelable(cancelable)
            .setView(viewBinding.root)
            .create()

        alertDialog.setCanceledOnTouchOutside(canceledOnTouchOutside)

        return alertDialog

    }
}