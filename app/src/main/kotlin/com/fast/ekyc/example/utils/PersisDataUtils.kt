package com.fast.ekyc.example.utils

import android.content.Context
import android.preference.PreferenceManager

object PersisDataUtils {
    fun savePersistData(
        context: Context,
        key: String,
        value: String,
    ) {
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit().putString(key, value).apply()
    }

    fun getPersistData(
        context: Context,
        key: String,
    ): String {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(key, "") ?: ""
    }
}