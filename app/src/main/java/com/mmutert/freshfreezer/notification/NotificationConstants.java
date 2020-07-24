package com.mmutert.freshfreezer.notification;

import androidx.core.app.NotificationCompat;

import java.util.concurrent.TimeUnit;


public class NotificationConstants {

    public static final String CHANNEL_ID = "FreshFreezerNotifications";

    public static final int NOTIFICATION_PRIORITY = NotificationCompat.PRIORITY_HIGH;


    public static final String KEY_ITEM_NAME = "item_name";
    public static final String KEY_ITEM_AMOUNT = "item_amount";
    public static final String KEY_ITEM_ID = "item_id";
    public static final String KEY_ITEM_AMOUNT_UNIT = "item_amount_unit";
    public static final String KEY_ITEM_BEST_BEFORE_DATE = "item_best_before_date";
    public static final String KEY_NOTIFICATION_OFFSET_AMOUNT = "notification_offset_amount";
    public static final String KEY_NOTIFICATION_OFFSET_UNIT = "notification_offset_unit";

    public static final TimeUnit NOTIFICATION_OFFSET_TIMEUNIT = TimeUnit.SECONDS;

}
