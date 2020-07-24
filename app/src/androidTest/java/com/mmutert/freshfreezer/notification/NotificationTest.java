package com.mmutert.freshfreezer.notification;

import android.content.Context;
import android.util.Log;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.work.Configuration;
import androidx.work.Data;
import androidx.work.ListenableWorker;
import androidx.work.WorkManager;
import androidx.work.testing.SynchronousExecutor;
import androidx.work.testing.TestListenableWorkerBuilder;
import androidx.work.testing.WorkManagerTestInitHelper;

import com.mmutert.freshfreezer.data.AmountUnit;
import com.mmutert.freshfreezer.data.FrozenItem;
import com.mmutert.freshfreezer.data.ItemNotification;
import com.mmutert.freshfreezer.data.TimeOffsetUnit;

import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.ExecutionException;


public class NotificationTest {


    private Context context;
    private WorkManager workManager;


    @Before
    public void setup() {

        context = ApplicationProvider.getApplicationContext();
        Configuration config = new Configuration.Builder()
                // Set log level to Log.DEBUG to
                // make it easier to see why tests failed
                .setMinimumLoggingLevel(Log.DEBUG)
                // Use a SynchronousExecutor to make it easier to write tests
                .setExecutor(new SynchronousExecutor())
                .build();

        // Initialize WorkManager for instrumentation tests.
        workManager = WorkManager.getInstance(context);
    }


    @Test
    public void testNotificationDisplay() throws ExecutionException, InterruptedException {

        FrozenItem item = createTestItem();
        ItemNotification testItemNotification = createTestItemNotification();

        Data inputData = NotificationHelper.createInputDataForItem(context, item, testItemNotification);

        TestListenableWorkerBuilder<NotificationWorker> builder =
                TestListenableWorkerBuilder.from(context, NotificationWorker.class);
        ListenableWorker worker = builder.setInputData(inputData)
                                         .build();
        ListenableWorker.Result result = worker.startWork().get();


    }


    public FrozenItem createTestItem() {

        FrozenItem item = new FrozenItem();
        item.setUnit(AmountUnit.LITERS);
        item.setAmount(10.532f);
        item.setName("NotificationTestItem");
        item.setBestBeforeDate(LocalDate.now().plusDays(2));
        item.setId(1);
        return item;
    }


    public ItemNotification createTestItemNotification() {

        return new ItemNotification(null, 1, TimeOffsetUnit.DAYS, 1);
    }
}


