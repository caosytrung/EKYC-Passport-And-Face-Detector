package com.fast.ekyc.utils

import android.graphics.RectF
import android.util.Log
import com.fast.ekyc.utils.extension.area

internal object CardIOUCaptureUtils {
    private var boxMap = mutableMapOf<Long, RectF>()
    private var oldRectF: RectF? = null
    var THRESHOLD = 0.92f
    private var MAX_TIME_IN_MILLI_SECOND = 1000

    fun reset() {
        oldRectF = null
        boxMap.clear()
    }

    fun checkValidFrame(box: RectF): Boolean {
        val currentTime = getCurrentTimeMillisecond()
        boxMap[currentTime] = box

        if (!isEnoughTimeToProcess()) {
            Log.d("isEnoughTimeToProcess", "false")
            return false
        }

        removeInvalidTime()

        return isAllFrameIOUValid().also {
            if (it) reset()
        }
    }

    private fun isEnoughTimeToProcess(): Boolean {
        val currentTime = getCurrentTimeMillisecond()
        val minTime = boxMap.keys.minOrNull() ?: 0

        if (currentTime - minTime < MAX_TIME_IN_MILLI_SECOND) {
            return false
        }

        return true
    }

    private fun removeInvalidTime() {
        val currentTime = getCurrentTimeMillisecond()

        boxMap = boxMap.filter {
            currentTime - it.key <= MAX_TIME_IN_MILLI_SECOND
        }.toMutableMap()
    }

    private fun isAllFrameIOUValid(): Boolean {
        if (boxMap.size < 2) return true

        val boxes = boxMap.entries.sortedBy { it.key }.map { it.value }
        for (i in 0..(boxes.size - 2)) {
            val firstRect = boxes[i]
            val secondRect = boxes[i + 1]

            val intersectRect = RectF()
            val isIntersect = intersectRect.setIntersect(firstRect, secondRect)

            if (!isIntersect) {
                return false
            }

            val intersectArea = intersectRect.area()

            val unionArea = firstRect.area() + secondRect.area() - intersectArea

            if (intersectArea / unionArea < THRESHOLD) {
                return false
            }
        }

        return true
    }

    private fun getCurrentTimeMillisecond() = System.currentTimeMillis()


    private fun check(rectF: RectF): Boolean {
        System.currentTimeMillis()
        if (oldRectF == null) {
            oldRectF = rectF
            return false
        }

        val isIntersect = rectF.setIntersect(rectF, oldRectF!!)

        if (!isIntersect) {
            oldRectF = rectF
            return false
        }

        val intersectArea = rectF.area()

        val unionArea = rectF.area() + oldRectF!!.area() - intersectArea

        if (intersectArea / unionArea < THRESHOLD) {
            oldRectF = rectF
            return false
        }


        oldRectF = null
        return true
    }
}