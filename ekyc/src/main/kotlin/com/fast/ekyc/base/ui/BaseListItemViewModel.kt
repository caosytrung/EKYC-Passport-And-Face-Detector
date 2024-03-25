package com.fast.ekyc.base.ui

internal abstract class BaseListItemViewModel<T : BaseListItem>(
    protected val item: T,
    protected val itemListener: BaseListItemListener<T>? = null
) {

    fun onItemClick() {
        itemListener?.onItemClick(item)
    }

}