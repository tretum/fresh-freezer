package com.mmutert.freshfreezer.data;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.mmutert.freshfreezer.data.converters.LocalDateTimeConverter;
import com.mmutert.freshfreezer.data.converters.OffsetUnitConverter;
import com.mmutert.freshfreezer.data.converters.UUIDConverter;

import org.joda.time.LocalDateTime;

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

    @TypeConverters(LocalDateTimeConverter.class)
    @ColumnInfo(name = "notify_on")
    private LocalDateTime notifyOn;

    @TypeConverters(OffsetUnitConverter.class)
    @ColumnInfo(name = "time_offset_unit")
    private TimeOffsetUnit timeOffsetUnit;

    @ColumnInfo(name = "offset_amount")
    private float offsetAmount;

    public ItemNotification(
            @NonNull final UUID notificationId,
            final long itemId,
            final LocalDateTime notifyOn,
            final TimeOffsetUnit timeOffsetUnit, final float offsetAmount) {
        this.notificationId = notificationId;
        this.itemId         = itemId;
        this.notifyOn       = notifyOn;
        this.timeOffsetUnit = timeOffsetUnit;
        this.offsetAmount   = offsetAmount;
    }

    @NonNull
    public UUID getNotificationId() {
        return notificationId;
    }

    public long getItemId() {
        return itemId;
    }

    public LocalDateTime getNotifyOn() {
        return notifyOn;
    }

    public TimeOffsetUnit getTimeOffsetUnit() {
        return timeOffsetUnit;
    }

    public float getOffsetAmount() {
        return offsetAmount;
    }
}
