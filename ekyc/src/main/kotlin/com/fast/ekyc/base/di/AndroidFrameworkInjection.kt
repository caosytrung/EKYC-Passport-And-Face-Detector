package com.fast.ekyc.base.di

import android.app.Activity
import android.app.Fragment
import android.app.Service
import android.content.BroadcastReceiver
import android.content.ContentProvider
import android.content.Context
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjection

object AndroidFrameworkInjection {
    private var androidInjector: AndroidInjector<Any>? = null

    fun setAndroidInjector(androidInjector: AndroidInjector<Any>) {
        AndroidFrameworkInjection.androidInjector = androidInjector
    }

    fun inject(activity: Activity) {
        androidInjector?.inject(activity as Any) ?: AndroidInjection.inject(activity)
    }

    fun inject(fragment: Fragment) {
        androidInjector?.inject(fragment as Any) ?: AndroidInjection.inject(fragment)
    }

    fun inject(fragment: androidx.fragment.app.Fragment) {
        AndroidSupportInjection.inject(fragment)
    }

    fun inject(service: Service) {
        androidInjector?.inject(service as Any) ?: AndroidInjection.inject(service)
    }

    fun inject(contentProvider: ContentProvider) {
        androidInjector?.inject(contentProvider as Any) ?: AndroidInjection.inject(contentProvider)
    }

    fun inject(
        receiver: BroadcastReceiver,
        context: Context
    ) {
        androidInjector?.inject(receiver) ?: AndroidInjection.inject(receiver, context)
    }
}