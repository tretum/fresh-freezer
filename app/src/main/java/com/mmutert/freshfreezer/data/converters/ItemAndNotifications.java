package com.mmutert.freshfreezer.data.converters;


import androidx.room.Embedded;
import androidx.room.Relation;

import com.mmutert.freshfreezer.data.FrozenItem;
import com.mmutert.freshfreezer.data.ItemNotification;

import java.util.List;


public class ItemAndNotifications {

    @Embedded
    public FrozenItem item;

    @Relation(parentColumn = "id", entityColumn = "item_id")
    public List<ItemNotification> notifications;
}
