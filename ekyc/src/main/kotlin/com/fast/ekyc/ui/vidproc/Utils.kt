package com.fast.ekyc.ui.vidproc

import android.media.MediaCodec
import android.os.Build
import android.util.Size
import androidx.annotation.RequiresApi
import kotlin.math.absoluteValue

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
internal fun getBestSupportedResolution(
    mediaCodec: MediaCodec,
    mime: String,
    preferredResolution: Size
): Size {

    // First check if exact combination supported
    if (mediaCodec.codecInfo.getCapabilitiesForType(mime)
            .videoCapabilities.isSizeSupported(
                preferredResolution.width,
                preferredResolution.height
            )
    )
        return preferredResolution

    // I try the resolutions suggested by docs for H.264 and VP8
    // https://developer.android.com/guide/topics/media/media-formats#video-encoding
    // TODO: find more supported resolutions
    val resolutions = arrayListOf(
        Size(176, 144),
        Size(320, 240),
        Size(320, 180),
        Size(640, 360),
        Size(720, 480),
        Size(1280, 720),
        Size(1920, 1080)
    )

    // I prefer similar resolution with similar aspect
    val pix = preferredResolution.width * preferredResolution.height
    val preferredAspect = preferredResolution.width.toFloat() / preferredResolution.height.toFloat()

    val nearestToFurthest = resolutions.sortedWith(
        compareBy(
            {
                pix - it.width * it.height
            },
            // First compare by aspect
            {
                val aspect = if (it.width < it.height) it.width.toFloat() / it.height.toFloat()
                else it.height.toFloat() / it.width.toFloat()
                (preferredAspect - aspect).absoluteValue
            })
    )

    for (size in nearestToFurthest) {
        if (mediaCodec.codecInfo.getCapabilitiesForType(mime)
                .videoCapabilities.isSizeSupported(size.width, size.height)
        )
            return size
    }

    throw RuntimeException("Couldn't find supported resolution")
}
