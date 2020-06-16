package com.mmutert.freshfreezer.data;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.mmutert.freshfreezer.data.converters.AmountUnitConverter;
import com.mmutert.freshfreezer.data.converters.DateConverter;

import java.util.Date;

@Entity(tableName = "items")
public class FrozenItem {

    @PrimaryKey(autoGenerate = true)
    @NonNull
    private long id;

    @NonNull
    private String name;
    private float amount;

    @TypeConverters(value = {AmountUnitConverter.class})
    private AmountUnit unit;

    @ColumnInfo(name = "frozen_date")
    @TypeConverters(value = {DateConverter.class})
    private Date frozenDate;

    @ColumnInfo(name = "best_before_date")
    @TypeConverters(value = {DateConverter.class})
    private Date bestBeforeDate;

    private String notes;


    @ColumnInfo(defaultValue = "false")
    private boolean archived;

    public FrozenItem() {

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

    public void setId(@NonNull long id) {
        this.id = id;
    }

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

    public Date getFrozenDate() {
        return frozenDate;
    }

    public void setFrozenDate(Date frozenDate) {
        this.frozenDate = frozenDate;
    }

    public Date getBestBeforeDate() {
        return bestBeforeDate;
    }

    public void setBestBeforeDate(Date bestBeforeDate) {
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
}
