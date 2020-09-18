package com.mmutert.freshfreezer.ui.itemlist

import com.mmutert.freshfreezer.data.StorageItem

interface ListItemClickedCallback {
    fun onClick(item: StorageItem)
}