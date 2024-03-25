package com.fast.ekyc.base.ui

internal interface BaseListItem {
    fun isSameAs(other: BaseListItem): Boolean
    fun isContentSameAs(other: BaseListItem): Boolean
}