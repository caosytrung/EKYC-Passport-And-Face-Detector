package com.fast.ekyc.di.component

import com.fast.ekyc.data.IDataManager
import com.fast.ekyc.data.config.request.EkycConfig
import com.fast.ekyc.di.module.DataModule
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        DataModule::class,
    ]
)
internal interface KycCoreComponent {

    fun create() : IDataManager

    @Component.Builder
    interface Builder {

        @BindsInstance
        fun config(config: EkycConfig): Builder

        fun build(): KycCoreComponent
    }
}