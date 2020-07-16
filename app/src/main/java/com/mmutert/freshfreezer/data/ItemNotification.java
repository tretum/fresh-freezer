package com.mmutert.freshfreezer.data;

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

import java.util.Objects;
import java.util.UUID;


@Entity(tableName = "notifications",
        foreignKeys = @ForeignKey(entity = FrozenItem.class,
                                  parentColumns = "id",
                                  childColumns = "item_id",
                                  onUpdate = ForeignKey.CASCADE),
        indices = @Index(name = "ItemIdIndex", value = "item_id"))
public class ItemNotification {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "notification_id")
    @TypeConverters(UUIDConverter.class)
    private UUID notificationId;

    @ColumnInfo(name = "item_id")
    private long itemId;

    @TypeConverters(OffsetUnitConverter.class)
    @ColumnInfo(name = "time_offset_unit")
    private TimeOffsetUnit timeOffsetUnit;

    @ColumnInfo(name = "offset_amount")
    private int offsetAmount;

    public ItemNotification(
            final UUID notificationId,
            final long itemId,
            final TimeOffsetUnit timeOffsetUnit,
            final int offsetAmount) {

        this.notificationId = notificationId;
        this.itemId         = itemId;
        this.timeOffsetUnit = timeOffsetUnit;
        this.offsetAmount   = offsetAmount;
    }

    public UUID getNotificationId() {
        return notificationId;
    }

    public long getItemId() {
        return itemId;
    }

    public TimeOffsetUnit getTimeOffsetUnit() {
        return timeOffsetUnit;
    }

    public int getOffsetAmount() {
        return offsetAmount;
    }

    public long getId() {
        return id;
    }

    public void setNotificationId(final UUID notificationId) {
        this.notificationId = notificationId;
    }


    public void setItemId(final long itemId) {

        this.itemId = itemId;
    }


    public void setId(final long id) {

        this.id = id;
    }


    @Override
    public boolean equals(final Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ItemNotification that = (ItemNotification) o;
        return id == that.id &&
                itemId == that.itemId &&
                offsetAmount == that.offsetAmount &&
                Objects.equals(notificationId, that.notificationId) &&
                timeOffsetUnit == that.timeOffsetUnit;
    }


    @Override
    public int hashCode() {

        return Objects.hash(id, notificationId, itemId, timeOffsetUnit, offsetAmount);
    }
}
