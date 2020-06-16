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
        String itemAmountUnit = inputData.getString(NotificationConstants.KEY_ITEM_AMOUNT_UNIT);

        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        Notification build = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_done_24px)
                .setContentTitle("Best Before Date coming up!")
                .setContentText("The date for " + itemName + " is coming up soon.")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText( "The date for " + itemName + " is coming up soon. There is still " + itemAmount + itemAmountUnit + " left.")
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
