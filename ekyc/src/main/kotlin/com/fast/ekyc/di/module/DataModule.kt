package com.fast.ekyc.di.module

import com.fast.ekyc.data.DataManager
import com.fast.ekyc.data.IDataManager
import com.fast.ekyc.data.config.request.EkycConfig
import com.fast.ekyc.data.network.api.KycApi
import com.fast.ekyc.data.network.api.KycApi.Companion.DEFAULT_GSON
import com.fast.ekyc.data.network.api.KycEndpoints.BASE_URL
import com.fast.ekyc.data.network.interceptor.KycInterceptor
import com.fast.ekyc.utils.apiBuilder
import dagger.Module
import dagger.Provides
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
internal object DataModule {

    @Provides
    @Singleton
    internal fun provideInterceptor(config: EkycConfig) =
        KycInterceptor("xxx", "xxx", config.isDebug)

    @Provides
    @Singleton
    internal fun provideKycService(
        config: EkycConfig,
        interceptor: KycInterceptor
    ) = apiBuilder(KycApi::class.java) {
        baseUrl = BASE_URL
        client {

            logging {
                HttpLoggingInterceptor.Level.BODY
            }

            interceptors = listOf(interceptor)
        }
        converter {
            factory = GsonConverterFactory.create(DEFAULT_GSON)
        }
    }

    @Provides
    @Singleton
    internal fun provideDataManger(config: EkycConfig, kycApi: KycApi): IDataManager =
        DataManager(config, kycApi)
}