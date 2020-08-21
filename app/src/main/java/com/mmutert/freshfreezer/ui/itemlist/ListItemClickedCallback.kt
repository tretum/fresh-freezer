package com.mmutert.freshfreezer.ui.itemlist

import com.mmutert.freshfreezer.data.FrozenItem

interface ListItemClickedCallback {
    fun onClick(item: FrozenItem?)
}