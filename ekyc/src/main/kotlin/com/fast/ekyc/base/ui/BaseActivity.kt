package com.fast.ekyc.base.ui

import android.content.Context
import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.fast.ekyc.R
import com.fast.ekyc.base.di.AndroidFrameworkInjection
import com.fast.ekyc.base.di.viewmodel.ViewModelProviderFactory
import com.fast.ekyc.utils.LoadingIndicatorBuilder
import com.fast.ekyc.utils.NetworkObserver
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import java.util.*
import javax.inject.Inject

internal abstract class BaseActivity<T : ViewDataBinding, VM : BaseViewModel> : AppCompatActivity(),
    HasAndroidInjector, NetworkObserver {
    @Inject
    lateinit var androidInjector: DispatchingAndroidInjector<Any>

    @Inject
    lateinit var viewModelFactory: ViewModelProviderFactory

    protected lateinit var viewDataBinding: T

    protected open val enableCustomLocale: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        performDependencyInjection()
        configureBeforeCreateView()
        super.onCreate(savedInstanceState)
        performDataBinding()
        initComponents()
    }

    private var progressDialog: AlertDialog? = null

    override fun androidInjector(): AndroidInjector<Any> = androidInjector

    abstract fun getViewModel(): VM

    abstract fun getBindingVariable(): Int

    @LayoutRes
    abstract fun getLayoutId(): Int

    abstract fun initComponents()

    open fun performDependencyInjection() {
        AndroidFrameworkInjection.inject(this)
    }

    private fun performDataBinding() {
        viewDataBinding = DataBindingUtil.setContentView(this, getLayoutId())
        viewDataBinding.setVariable(getBindingVariable(), getViewModel())
        viewDataBinding.lifecycleOwner = this
        viewDataBinding.executePendingBindings()
    }

    override fun onConnectivityChange(isOnline: Boolean) {

    }

    protected open fun configureBeforeCreateView() {}

    open fun getLanguageCode(context: Context): String = Locale.getDefault().language


    /**
     * default loading behavior, descendants can override it to have it own behavior
     *
     * If you override it, remember to override [hideLoading] too
     */
    protected open fun showActivityLoading() {
        if (progressDialog == null) {
            progressDialog = showProgressDialog()
        } else if (progressDialog?.isShowing == false) {
            progressDialog!!.show()
        }
    }

    private fun showProgressDialog(cancelable: Boolean = false): AlertDialog {
        return LoadingIndicatorBuilder(this)
            .withCancelable(cancelable)
            .withCanceledOnTouchOutside(cancelable)
            .withLoadingContent(getString(R.string.kyc_loading_text))
            .build().apply {
                show()
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        hideActivityLoading()
    }

    /**
     * cleanup any loading behavior has taken before
     */
    protected open fun hideActivityLoading() {
        progressDialog?.dismiss()
        progressDialog = null
    }

}
