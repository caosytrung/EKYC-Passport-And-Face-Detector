package com.fast.ekyc.base.ui

import androidx.lifecycle.Observer

/**
 * Used as a wrapper for data that is exposed via a LiveData that represents an event.
 *
 * reference:
 * https://medium.com/androiddevelopers/livedata-with-snackbar-navigation-and-other-events-the-singleliveevent-case-ac2622673150
 */
open class SingleHandlerEvent<out T>(private val content: T) {

    private var hasBeenHandled = false

    /**
     * Returns the content and prevents its use again.
     */
    fun getContentIfNotHandled(): T? {
        return if (hasBeenHandled) {
            null
        } else {
            hasBeenHandled = true
            content
        }
    }

    /**
     * Returns the content, even if it's already been handled.
     */
    fun peekContent(): T = content

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as SingleHandlerEvent<T>

        return content == other.content && hasBeenHandled == other.hasBeenHandled
    }
}

/**
 * An [Observer] for [SingleHandlerEvent]s, simplifying the pattern of checking if the [SingleHandlerEvent]'s content has
 * already been handled.
 *
 * [onEventUnhandledContent] is *only* called if the [SingleHandlerEvent]'s contents has not been handled.
 */
class SingleHandlerEventObserver<T>(
    private val onEventUnhandledContent: (T) -> Unit
) : Observer<SingleHandlerEvent<T>> {
    override fun onChanged(event: SingleHandlerEvent<T>?) {
        event?.getContentIfNotHandled()
            ?.let { value ->
                onEventUnhandledContent(value)
            }
    }
}