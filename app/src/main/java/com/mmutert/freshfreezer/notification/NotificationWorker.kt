package com.mmutert.freshfreezer.notification

import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.navigation.NavDeepLinkBuilder
import androidx.preference.PreferenceManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.mmutert.freshfreezer.R
import com.mmutert.freshfreezer.data.AmountUnit
import com.mmutert.freshfreezer.data.AmountUnit.Companion.getFormatterForUnit

class NotificationWorker(private val context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    override fun doWork(): Result {
        val inputData = inputData
        val itemName = inputData.getString(NotificationConstants.KEY_ITEM_NAME)
        val itemAmount = inputData.getFloat(NotificationConstants.KEY_ITEM_AMOUNT, 0f)
        val itemId = inputData.getLong(NotificationConstants.KEY_ITEM_ID, 0)
        val itemAmountUnit = AmountUnit.valueOf(inputData.getString(NotificationConstants.KEY_ITEM_AMOUNT_UNIT)!!)

//        Intent intent = new Intent(context, MainActivity.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        val args = Bundle()
        args.putLong("itemId", itemId)

        val pendingIntent = NavDeepLinkBuilder(context)
                .setGraph(R.navigation.nav_graph)
                .setDestination(R.id.addItemFragment)
                .setArguments(args)
                .createPendingIntent()
        val formatterForUnit = getFormatterForUnit(itemAmountUnit)
        val amountFormatted = formatterForUnit.format(itemAmount.toDouble())
        val amountUnitFormatted = context.resources.getString(itemAmountUnit.stringResId)
        val offsetAmount = inputData.getString(NotificationConstants.KEY_NOTIFICATION_OFFSET_AMOUNT)
        val offsetUnit = inputData.getString(NotificationConstants.KEY_NOTIFICATION_OFFSET_UNIT)
        val titleText = context.getString(
                R.string.best_before_notification_title,
                itemName,
                offsetAmount,
                offsetUnit
        )
        val formattedTitleText: Spannable = SpannableString(titleText)
        formattedTitleText.setSpan(
                StyleSpan(Typeface.BOLD),
                0,
                titleText.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        val bestBeforeDateFormatted = inputData.getString(NotificationConstants.KEY_ITEM_BEST_BEFORE_DATE)
        val content = context.getString(
                R.string.best_before_notification_text_short,
                bestBeforeDateFormatted,
                amountFormatted,
                amountUnitFormatted
        )
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val persistentNotification = sharedPreferences.getBoolean(
                context.getString(R.string.pref_persistent_notifications_key),
                false
        )
        val autoCancel = !persistentNotification

        val notification = NotificationCompat.Builder(context, NotificationConstants.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notifications_none_24px)
                .setContentTitle(formattedTitleText)
                .setContentText(content)
                .setPriority(NotificationConstants.NOTIFICATION_PRIORITY)
                .setContentIntent(pendingIntent)
                .setAutoCancel(autoCancel)
                .build()

        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(itemId.toInt(), notification)

        return Result.success()
    }
}