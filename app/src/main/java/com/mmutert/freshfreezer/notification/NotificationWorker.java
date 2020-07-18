package com.mmutert.freshfreezer.notification;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
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

        Notification build = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_done_24px)
                .setContentTitle(context.getString(R.string.best_before_notification_title))
                .setContentText(context.getString(R.string.best_before_notification_text_short, itemName))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(
                        context.getResources()
                               .getQuantityString(
                                       R.plurals.best_before_notification_text_long,
                                       Math.round(itemAmount),
                                       itemName,
                                       amountFormatted,
                                       amountUnitFormatted
                               ))
                )
                .setPriority(NotificationConstants.NOTIFICATION_PRIORITY)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build();

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(itemId, build);

        return Result.success();
    }


}
