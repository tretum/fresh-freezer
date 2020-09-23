package com.mmutert.freshfreezer.notification;

import android.content.Context;
import android.util.Log;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.work.Configuration;
import androidx.work.testing.SynchronousExecutor;
import androidx.work.testing.WorkManagerTestInitHelper;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.ExecutionException;

import static org.hamcrest.Matchers.is;


/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {


    public static final String TAG = "NotificationTest";

    @Before
    public void setup() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        Configuration config = new Configuration.Builder()
                // Set log level to Log.DEBUG to
                // make it easier to see why tests failed
                .setMinimumLoggingLevel(Log.DEBUG)
                // Use a SynchronousExecutor to make it easier to write tests
                .setExecutor(new SynchronousExecutor())
                .build();

        // Initialize WorkManager for instrumentation tests.
        WorkManagerTestInitHelper.initializeTestWorkManager(
                context, config);
    }


    @Test
    @Ignore
    public void testNotificationLayoutAndText() throws ExecutionException, InterruptedException {

//        Log.d(TAG, "Starting notification test");
//
//        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
//        FrozenItem frozenItem = new FrozenItem();
//        frozenItem.setUnit(AmountUnit.LITERS);
//        frozenItem.setAmount(10.532f);
//        frozenItem.setName("NotificationTestItem");
//
//        Log.d(TAG, "Scheduling...");
//        Log.d(TAG, "Current time is ..." + TimeHelper.getCurrentDateTimeLocalized().toString());
//
//        TimeUnit timeUnit = TimeUnit.SECONDS;
//        LocalDateTime startDate = TimeHelper.getCurrentDateTimeLocalized();
//        LocalDateTime scheduledOn = TimeHelper.getCurrentDateTimeLocalized().plusSeconds(1);
//        long offset = NotificationHelper.calculateOffset(timeUnit, startDate, scheduledOn);
//
//        OneTimeWorkRequest workRequest = NotificationHelper.createWorkRequest(frozenItem, offset, timeUnit);
//        WorkManager workManager = WorkManager.getInstance(appContext);
//        // Get the TestDriver
//        TestDriver testDriver = WorkManagerTestInitHelper.getTestDriver(appContext);
//
//        // Enqueue and wait for result. This also runs the Worker synchronously
//        // because we are using a SynchronousExecutor.
//        workManager.enqueue(workRequest).getResult().get();
//
//        // Tells the WorkManager test framework that initial delays are now met.
//        testDriver.setInitialDelayMet(workRequest.getId());
//
//        // Get WorkInfo and outputData
//        WorkInfo workInfo = workManager.getWorkInfoById(workRequest.getId()).get();
//
//        // Assert
//        assertThat(workInfo.getState(), is(WorkInfo.State.SUCCEEDED));
    }
}
