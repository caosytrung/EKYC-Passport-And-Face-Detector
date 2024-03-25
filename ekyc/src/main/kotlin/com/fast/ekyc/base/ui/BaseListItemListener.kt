package com.fast.ekyc.base.ui

internal interface BaseListItemListener<T> {
    /**
     * Call when a list item got clicked
     *
     * @param item the item that is clicked
     *
     * @see [BaseListItemViewModel.itemListener]
     */
    fun onItemClick(item: T)
}