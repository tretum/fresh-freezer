package com.mmutert.freshfreezer.notification;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.preference.PreferenceManager;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.mmutert.freshfreezer.MainActivity;
import com.mmutert.freshfreezer.R;
import com.mmutert.freshfreezer.data.AmountUnit;

import java.text.NumberFormat;

import static com.mmutert.freshfreezer.notification.NotificationConstants.CHANNEL_ID;


public class NotificationWorker extends Worker {

    @NonNull
    private final Context context;


    public NotificationWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {

        super(context, workerParams);
        this.context = context;
    }


    @NonNull
    @Override
    public Result doWork() {

        Data inputData = getInputData();
        String itemName = inputData.getString(NotificationConstants.KEY_ITEM_NAME);
        float itemAmount = inputData.getFloat(NotificationConstants.KEY_ITEM_AMOUNT, 0);
        int itemId = inputData.getInt(NotificationConstants.KEY_ITEM_ID, 0);
        AmountUnit itemAmountUnit = AmountUnit.valueOf(inputData.getString(NotificationConstants.KEY_ITEM_AMOUNT_UNIT));

        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        NumberFormat formatterForUnit = AmountUnit.getFormatterForUnit(itemAmountUnit);
        String amountFormatted = formatterForUnit.format(itemAmount);
        String amountUnitFormatted = context.getResources().getString(itemAmountUnit.getStringResId());

        String offsetAmount = inputData.getString(NotificationConstants.KEY_NOTIFICATION_OFFSET_AMOUNT);
        String offsetUnit = inputData.getString(NotificationConstants.KEY_NOTIFICATION_OFFSET_UNIT);

        String titleText = context.getString(
                R.string.best_before_notification_title,
                itemName,
                offsetAmount,
                offsetUnit
        );
        Spannable formattedTitleText = new SpannableString(titleText);
        formattedTitleText.setSpan(
                new StyleSpan(android.graphics.Typeface.BOLD),
                0,
                titleText.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        );

        String bestBeforeDateFormatted = inputData.getString(NotificationConstants.KEY_ITEM_BEST_BEFORE_DATE);

        String content = context.getString(
                R.string.best_before_notification_text_short,
                bestBeforeDateFormatted,
                amountFormatted,
                amountUnitFormatted
        );

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean persistentNotification = sharedPreferences.getBoolean(
                context.getString(R.string.pref_persistent_notifications_key),
                false
        );
        boolean autoCancel = !persistentNotification;
        Notification build = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notifications_none_24px)
                .setContentTitle(formattedTitleText)
                .setContentText(content)
                .setPriority(NotificationConstants.NOTIFICATION_PRIORITY)
                .setContentIntent(pendingIntent)
                .setAutoCancel(autoCancel)
                .build();

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(itemId, build);

        return Result.success();
    }


}
