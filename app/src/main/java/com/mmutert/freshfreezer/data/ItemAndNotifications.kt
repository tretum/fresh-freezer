package com.mmutert.freshfreezer.data

import androidx.room.Embedded
import androidx.room.Relation

class ItemAndNotifications (
    @Embedded
    val item: FrozenItem,

    @Relation(parentColumn = "id", entityColumn = "item_id")
    val notifications: List<ItemNotification>
)