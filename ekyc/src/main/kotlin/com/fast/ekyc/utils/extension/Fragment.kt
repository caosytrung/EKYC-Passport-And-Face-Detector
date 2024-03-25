package com.fast.ekyc.utils.extension

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.view.Gravity
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.fast.ekyc.R
import com.fast.ekyc.utils.LoadingIndicatorBuilder


internal fun Fragment.showToast(
    @StringRes textId: Int,
    gravity: Int = Gravity.BOTTOM,
    duration: Int = Toast.LENGTH_LONG,
    shouldRemainWhenFragmentDismissed: Boolean = false
) {
    context?.let { ctx ->
        Toast.makeText(ctx, textId, duration).show()
    }
}

internal fun Fragment.makePhoneCall(phone: String) {
    try {
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
        startActivity(intent)
    } catch (_: Throwable) {
    }
}

internal fun Fragment.hideKeyboard() {
    activity?.let {
        val imm = it.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        imm?.hideSoftInputFromWindow(it.window.currentFocus?.windowToken, 0)
    }
}

internal fun Fragment.setStatusBarColor(color: Int) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        activity?.window?.statusBarColor = color
    }
}

internal fun Fragment.showDialogNoTrack(
    title: Int,
    message: String,
    positiveButtonTitle: Int,
    positiveButtonAction: (DialogInterface, Int) -> Unit,
    negativeButtonTitle: Int,
    negativeButtonAction: (DialogInterface, Int) -> Unit,
    cancelable: Boolean = true
): AlertDialog? {
    return if (context != null) {
        AlertDialog.Builder(context!!)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(positiveButtonTitle, positiveButtonAction)
            .setNegativeButton(negativeButtonTitle, negativeButtonAction)
            .setCancelable(cancelable)
            .show()
    } else null
}

internal fun Fragment.showMessageDialogNoTrack(
    title: Int,
    message: String,
    positiveButtonTitle: Int,
    positiveButtonAction: (DialogInterface, Int) -> Unit,
    cancelable: Boolean = true
): AlertDialog? {
    return if (context != null) {
        AlertDialog.Builder(context!!)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(positiveButtonTitle, positiveButtonAction)
            .setCancelable(cancelable)
            .show()
    } else null
}

internal fun Fragment.showMessageDialogNoTrack(
    title: Int,
    @StringRes messageId: Int,
    positiveButtonTitle: Int,
    positiveButtonAction: (DialogInterface, Int) -> Unit,
    cancelable: Boolean = true
): AlertDialog? {
    return if (context != null) {
        showMessageDialogNoTrack(
            title,
            context!!.getString(messageId),
            positiveButtonTitle,
            positiveButtonAction,
            cancelable
        )
    } else null
}

internal fun Fragment.showProgressDialog(cancelable: Boolean = false): AlertDialog? {
    return if (context != null) {
        LoadingIndicatorBuilder(context!!)
            .withCancelable(cancelable)
            .withCanceledOnTouchOutside(cancelable)
            .withLoadingContent(getString(R.string.kyc_loading_text))
            .build().apply {
                show()
            }
    } else null
}

internal fun Fragment.hasPermission(permission: String): Boolean {
    return if (context != null) {
        val status = ContextCompat.checkSelfPermission(context!!, permission)
        return status == PackageManager.PERMISSION_GRANTED
    } else false
}

internal fun Activity.hasPermission(permission: String): Boolean {
    val status = ContextCompat.checkSelfPermission(this, permission)
    return status == PackageManager.PERMISSION_GRANTED
}


internal fun Fragment.requirePermission(
    permission: String,
    permissionExplanation: String,
    onPermissionGranted: (String) -> Unit
) {
    if (!hasPermission(permission)) {
        if (shouldShowRequestPermissionRationale(permission)) {
            showPermissionRequestExplanation(permission, permissionExplanation)
        } else {
            requestPermission(permission)
        }
    } else {
        onPermissionGranted(permission)
    }
}

internal fun Fragment.requestPermission(permission: String) {
    requestPermissions(arrayOf(permission), permission.hashCode())
}

private const val RESULT = 10001
internal fun Activity.requestPermission(permission: String) {
    ActivityCompat.requestPermissions(this, arrayOf(permission), RESULT)
}

internal fun Activity.requestPermissions(permissions: List<String>) {
    ActivityCompat.requestPermissions(this, permissions.toTypedArray(), RESULT)
}

internal fun Fragment.onRequestPermissionsResult(
    requestCode: Int,
    permissions: Array<out String>,
    grantResults: IntArray,
    onPermissionGranted: (String) -> Unit,
    onPermissionDenied: (String) -> Unit
) {
    val permission = permissions.find { it.hashCode() == requestCode }
    if (permission != null) {
        if ((grantResults.isNotEmpty() && grantResults.first() == PackageManager.PERMISSION_GRANTED)) {
            onPermissionGranted(permission)
        } else {
            onPermissionDenied(permission)
        }
    }
}

internal fun Activity.onRequestPermissionsResult(
    requestCode: Int,
    permissions: Array<out String>,
    grantResults: IntArray,
    onPermissionGranted: () -> Unit,
    onPermissionDenied: () -> Unit
) {

    if (requestCode != RESULT) return

    if (grantResults.any { it == PackageManager.PERMISSION_DENIED }) {
        onPermissionDenied()
    } else {
        onPermissionGranted()
    }

}

internal fun Fragment.showPermissionRequestExplanation(
    permission: String,
    permissionExplanation: String,
) {
    showDialogNoTrack(
        title = R.string.kyc_notice,
        message = permissionExplanation,
        positiveButtonTitle = R.string.kyc_ok,
        positiveButtonAction = { _, _ ->
            requestPermission(permission)
        },
        negativeButtonTitle = R.string.kyc_close,
        negativeButtonAction = { _, _ -> }
    )
}