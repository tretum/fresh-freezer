package com.mmutert.freshfreezer.notification

import androidx.core.app.NotificationCompat
import java.util.concurrent.TimeUnit

object NotificationConstants {
    const val CHANNEL_ID = "FreshFreezerNotifications"
    const val NOTIFICATION_PRIORITY = NotificationCompat.PRIORITY_HIGH
    const val KEY_ITEM_NAME = "item_name"
    const val KEY_ITEM_AMOUNT = "item_amount"
    const val KEY_ITEM_ID = "item_id"
    const val KEY_ITEM_AMOUNT_UNIT = "item_amount_unit"
    const val KEY_ITEM_BEST_BEFORE_DATE = "item_best_before_date"
    const val KEY_NOTIFICATION_OFFSET_AMOUNT = "notification_offset_amount"
    const val KEY_NOTIFICATION_OFFSET_UNIT = "notification_offset_unit"
    val NOTIFICATION_OFFSET_TIME_UNIT = TimeUnit.SECONDS
}