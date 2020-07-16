package com.mmutert.freshfreezer.data;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.mmutert.freshfreezer.data.converters.AmountUnitConverter;
import com.mmutert.freshfreezer.data.converters.LocalDateConverter;
import com.mmutert.freshfreezer.data.converters.LocalDateTimeConverter;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import java.util.Objects;


@Entity(tableName = "items")
public class FrozenItem {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @NonNull
    private String name;

    private float amount;

    @TypeConverters(value = {AmountUnitConverter.class})
    private AmountUnit unit;

    @ColumnInfo(name = "frozen_date")
    @TypeConverters(value = {LocalDateConverter.class})
    private LocalDate frozenAtDate;

    @ColumnInfo(name = "best_before_date")
    @TypeConverters(value = {LocalDateConverter.class})
    private LocalDate bestBeforeDate;

    @ColumnInfo(name = "item_creation_date")
    @TypeConverters(value = {LocalDateTimeConverter.class})
    private LocalDateTime itemCreationDate;

    @ColumnInfo(name = "last_changed_at_date")
    @TypeConverters(value = {LocalDateTimeConverter.class})
    private LocalDateTime lastChangedAtDate;

    private String notes;


    @ColumnInfo(defaultValue = "false")
    private boolean archived;

    public FrozenItem() {
        name = "";
    }

    public AmountUnit getUnit() {
        return unit;
    }

    public void setUnit(AmountUnit unit) {
        this.unit = unit;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }

    public float getAmount() {
        return amount;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }

    public LocalDate getFrozenAtDate() {
        return frozenAtDate;
    }

    public void setFrozenAtDate(LocalDate frozenDate) {
        this.frozenAtDate = frozenDate;
    }

    public LocalDate getBestBeforeDate() {
        return bestBeforeDate;
    }

    public void setBestBeforeDate(LocalDate bestBeforeDate) {
        this.bestBeforeDate = bestBeforeDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public boolean isArchived() {
        return archived;
    }

    public void setArchived(final boolean archived) {
        this.archived = archived;
    }

    public LocalDateTime getItemCreationDate() {
        return itemCreationDate;
    }

    public void setItemCreationDate(@NonNull final LocalDateTime itemCreationDate) {
        this.itemCreationDate = itemCreationDate;
    }

    public LocalDateTime getLastChangedAtDate() {
        return lastChangedAtDate;
    }

    public void setLastChangedAtDate(@NonNull final LocalDateTime lastChangedAtDate) {
        this.lastChangedAtDate = lastChangedAtDate;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FrozenItem that = (FrozenItem) o;
        return id == that.id &&
                Float.compare(that.amount, amount) == 0 &&
                archived == that.archived &&
                name.equals(that.name) &&
                unit == that.unit &&
                Objects.equals(frozenAtDate, that.frozenAtDate) &&
                Objects.equals(bestBeforeDate, that.bestBeforeDate) &&
                itemCreationDate.equals(that.itemCreationDate) &&
                lastChangedAtDate.equals(that.lastChangedAtDate) &&
                Objects.equals(notes, that.notes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                id,
                name,
                amount,
                unit,
                frozenAtDate,
                bestBeforeDate,
                itemCreationDate,
                lastChangedAtDate,
                notes,
                archived
        );
    }
}
