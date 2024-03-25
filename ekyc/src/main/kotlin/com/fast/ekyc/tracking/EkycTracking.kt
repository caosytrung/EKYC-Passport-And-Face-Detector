package com.fast.ekyc.tracking

interface EkycTracking {
    fun createEventAndTrack(
        objectName: String,
        eventSrc: EventSrc,
        objectType: ObjectType,
        action: EventAction,
        eventValue: Map<String, Any>? = null
    )
}