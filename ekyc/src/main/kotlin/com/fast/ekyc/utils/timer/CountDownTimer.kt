package com.fast.ekyc.utils.timer

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.ticker
import kotlinx.coroutines.launch

internal abstract class CountDownTimer(
    private val millisDuration: Long,
    private val millisInterval: Long
) {
    private var countDownJob: Job? = null

    @ObsoleteCoroutinesApi
    fun start(scope: CoroutineScope): CountDownTimer {
        synchronized(this) {
            cancel()
            countDownJob = scope.launch {
                var remainingDuration = millisDuration
                val tickerChannel = ticker(millisInterval, 0)

                for (event in tickerChannel) {
                    onTick(remainingDuration)

                    remainingDuration -= millisInterval
                    if (remainingDuration < 0) {
                        break
                    }
                }
                tickerChannel.cancel()
                onFinish()
            }
        }

        return this
    }

    fun cancel() {
        synchronized(this) {
            countDownJob?.cancel()
            countDownJob = null
        }
    }

    abstract fun onTick(remainingMillis: Long)

    abstract fun onFinish()
}