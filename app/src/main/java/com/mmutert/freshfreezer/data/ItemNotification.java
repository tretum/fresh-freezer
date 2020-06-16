package com.mmutert.freshfreezer.data;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;


@Entity(tableName = "notifications",
        foreignKeys = @ForeignKey(entity = FrozenItem.class,
                                  parentColumns = "id",
                                  childColumns = "item_id",
                                  onUpdate = ForeignKey.CASCADE),
        indices = @Index(name = "ItemIdIndex", value = "item_id"))
public class ItemNotification {

    @PrimaryKey()
    @NonNull
    @ColumnInfo(name = "notification_id")
    private String notificationId;

    @ColumnInfo(name = "item_id")
    private long itemId;

    public ItemNotification(@NonNull final String notificationId, final long itemId) {
        this.notificationId = notificationId;
        this.itemId = itemId;
    }

    @NonNull
    public String getNotificationId() {
        return notificationId;
    }

    public long getItemId() {
        return itemId;
    }
}
