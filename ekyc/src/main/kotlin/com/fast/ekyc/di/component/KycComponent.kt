package com.fast.ekyc.di.component

import android.content.Context
import com.fast.ekyc.data.config.request.EkycConfig
import com.fast.ekyc.di.module.DataModule
import com.fast.ekyc.di.module.FragmentModule
import com.fast.ekyc.di.module.ViewModelModule
import com.fast.ekyc.ui.main.MainActivity
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjectionModule
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        AndroidInjectionModule::class,
        ViewModelModule::class,
        FragmentModule::class,
        DataModule::class,
    ]
)
internal interface KycComponent {

    fun injectMainActivity(activity: MainActivity)

    @Component.Builder
    interface Builder {

        @BindsInstance
        fun context(context: Context): Builder

        @BindsInstance
        fun config(config: EkycConfig): Builder

        fun build(): KycComponent
    }
}