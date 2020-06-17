package com.mmutert.freshfreezer.data;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.mmutert.freshfreezer.data.converters.UUIDConverter;

import java.util.UUID;


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
    @TypeConverters(UUIDConverter.class)
    private UUID notificationId;

    @ColumnInfo(name = "item_id")
    private long itemId;

    public ItemNotification(@NonNull final UUID notificationId, final long itemId) {
        this.notificationId = notificationId;
        this.itemId = itemId;
    }

    @NonNull
    public UUID getNotificationId() {
        return notificationId;
    }

    public long getItemId() {
        return itemId;
    }
}
