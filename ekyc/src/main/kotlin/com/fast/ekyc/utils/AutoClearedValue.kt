package com.fast.ekyc.utils

import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.observe
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * A lazy property that gets cleaned up when the fragment's view is destroyed.
 *
 * Accessing this variable while the fragment's view is destroyed will throw NPE.
 */
internal class AutoClearedValue<T : Any>(val fragment: Fragment) : ReadWriteProperty<Fragment, T> {
    private var value: T? = null

    init {
        fragment.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {}
            override fun onResume(owner: LifecycleOwner) {}
            override fun onPause(owner: LifecycleOwner) {}
            override fun onStop(owner: LifecycleOwner) {}
            override fun onDestroy(owner: LifecycleOwner) {}

            override fun onCreate(owner: LifecycleOwner) {
                fragment.viewLifecycleOwnerLiveData.observe(fragment) { viewLifecycleOwner ->
                    viewLifecycleOwner?.lifecycle?.addObserver(object : DefaultLifecycleObserver {
                        override fun onStart(owner: LifecycleOwner) {}
                        override fun onResume(owner: LifecycleOwner) {}
                        override fun onPause(owner: LifecycleOwner) {}
                        override fun onStop(owner: LifecycleOwner) {}
                        override fun onCreate(owner: LifecycleOwner) {}

                        override fun onDestroy(owner: LifecycleOwner) {
                            value = null
                        }
                    })
                }
            }
        })
    }

    override fun getValue(thisRef: Fragment, property: KProperty<*>): T {
        return value ?: throw IllegalStateException(
            "should never call auto-cleared-value get when it might not be available"
        )
    }

    override fun setValue(thisRef: Fragment, property: KProperty<*>, value: T) {
        this.value = value
    }
}

/**
 * Creates an [AutoClearedValue] associated with this fragment.
 */
internal fun <T : Any> Fragment.autoCleared() = AutoClearedValue<T>(this)