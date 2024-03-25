package com.fast.ekyc.base.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.annotation.ColorRes
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.observe
import com.fast.ekyc.R
import com.fast.ekyc.base.di.AndroidFrameworkInjection
import com.fast.ekyc.base.di.viewmodel.ViewModelProviderFactory
import com.fast.ekyc.ui.main.MainActivity
import com.fast.ekyc.ui.main.MainViewModel
import com.fast.ekyc.utils.autoCleared
import com.fast.ekyc.utils.extension.showProgressDialog
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import javax.inject.Inject

internal abstract class BaseFragment<T : ViewDataBinding, VM : BaseViewModel> : Fragment(),
    HasAndroidInjector {
    @Inject
    lateinit var androidInjector: DispatchingAndroidInjector<Any>

    @Inject
    lateinit var viewModelFactory: ViewModelProviderFactory

    protected val mainViewModel: MainViewModel by activityViewModels { viewModelFactory }

    private var progressDialog: AlertDialog? = null

    protected var viewDataBinding by autoCleared<T>()

    private var componentActivity: ComponentActivity? = null

    override fun onAttach(context: Context) {
        performDependencyInjection()
        super.onAttach(context)
        if (context is ComponentActivity) {
            componentActivity = context
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(false)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewDataBinding = DataBindingUtil.inflate(inflater, getLayoutId(), container, false)
        return viewDataBinding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        performDataBinding()

        if (isShowDefaultLoading()){
            getViewModel().getState().observe(viewLifecycleOwner){
                if (it.isLoading()){
                    showLoading()
                } else{
                    hideLoading()
                }
            }
        }

        initComponents()

        viewLifecycleOwnerLiveData.observe(viewLifecycleOwner) { owner ->
            componentActivity?.onBackPressedDispatcher?.addCallback(
                owner, isEnableCustomBackPressed()
            ) { onBackPressed() }

            owner?.lifecycle?.addObserver(object : DefaultLifecycleObserver {

                override fun onCreate(owner: LifecycleOwner) {}
                override fun onStart(owner: LifecycleOwner) {}
                override fun onResume(owner: LifecycleOwner) {}
                override fun onPause(owner: LifecycleOwner) {}
                override fun onStop(owner: LifecycleOwner) {}


                override fun onDestroy(owner: LifecycleOwner) {
                    clearComponents()
                }
            })
        }
    }

    @ColorRes
    open fun getStatusBarColor(): Int {
        return R.color.kyc_color_transparent
    }

    open fun isShowDefaultLoading() = true

    open fun isEnableCustomBackPressed() = false

    open fun onBackPressed() {}

    open fun clearComponents() {}

    override fun onDetach() {
        componentActivity = null

        hideLoading()
        super.onDetach()
    }

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
        viewDataBinding.setVariable(getBindingVariable(), getViewModel())
        viewDataBinding.lifecycleOwner = viewLifecycleOwner
        viewDataBinding.executePendingBindings()
    }

    /**
     * default loading behavior, descendants can override it to have it own behavior
     *
     * If you override it, remember to override [hideLoading] too
     */
    protected open fun showLoading() {
        context?.let {
            if (progressDialog == null) {
                progressDialog = showProgressDialog()
            } else if (progressDialog?.isShowing == false) {
                progressDialog!!.show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        hideLoading()
    }

    /**
     * cleanup any loading behavior has taken before
     */
    protected open fun hideLoading() {
        progressDialog?.dismiss()
        progressDialog = null
    }

    protected fun getMainActivity() = activity as? MainActivity
}
