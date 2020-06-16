package com.mmutert.freshfreezer.data;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;


@Entity(tableName = "notifications",
        foreignKeys = @ForeignKey(entity = FrozenItem.class,
                                  parentColumns = "id",
                                  childColumns = "itemId",
                                  onUpdate = ForeignKey.CASCADE))
public class ItemNotification {

    @PrimaryKey()
    @NonNull
    @ColumnInfo(name = "notification_id")
    private int notificationId;

    @ColumnInfo(name = "item_id")
    private long itemId;

    public ItemNotification() {
    }

    public int getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(final int notificationId) {
        this.notificationId = notificationId;
    }

    public long getItemId() {
        return itemId;
    }

    public void setItemId(final long itemId) {
        this.itemId = itemId;
    }
}
