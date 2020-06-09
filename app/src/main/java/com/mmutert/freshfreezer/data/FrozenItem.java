package com.mmutert.freshfreezer.data;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverter;
import androidx.room.TypeConverters;

import java.util.Date;

@Entity(tableName = "items")
public class FrozenItem {

    @PrimaryKey(autoGenerate = true)
    @NonNull
    private long id;

    private String name;
    private int amount;

    @ColumnInfo(name = "frozen_date")
    @TypeConverters(value = {DateConverter.class})
    private Date frozenDate;

    @ColumnInfo(name = "best_before_date")
    @TypeConverters(value = {DateConverter.class})
    private Date bestBeforeDate;


    public FrozenItem() {

    }

    public FrozenItem(@NonNull long id, String name, int amount, Date frozenDate, Date bestBeforeDate) {
        this.id = id;
        this.name = name;
        this.amount = amount;
        this.frozenDate = frozenDate;
        this.bestBeforeDate = bestBeforeDate;
    }

    public static class DateConverter {

        @TypeConverter
        public static Date toDate(Long dateLong){
            return dateLong == null ? null: new Date(dateLong);
        }

        @TypeConverter
        public static Long fromDate(Date date){
            return date == null ? null : date.getTime();
        }
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

    public void setName(String name) {
        this.name = name;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
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
}
