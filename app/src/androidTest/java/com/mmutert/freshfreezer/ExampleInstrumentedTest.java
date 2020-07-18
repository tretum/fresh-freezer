package com.mmutert.freshfreezer;

import android.content.Context;
import android.util.Log;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.mmutert.freshfreezer.data.AmountUnit;
import com.mmutert.freshfreezer.data.FrozenItem;
import com.mmutert.freshfreezer.notification.NotificationHelper;

import org.joda.time.LocalDateTime;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {


    public static final String TAG = "NotificationTest";


    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();

        assertEquals("com.mmutert.freshfreezer", appContext.getPackageName());
    }

    @Test
    public void testNotificationLayoutAndText() {

        Log.d(TAG, "Starting notification test");
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        FrozenItem frozenItem = new FrozenItem();
        frozenItem.setUnit(AmountUnit.LITERS);
        frozenItem.setAmount(10.532f);
        frozenItem.setName("NotificationTestItem");
        Log.d(TAG, "Scheduling...");
        assertTrue(true);
        assertEquals("com.mmutert.freshfreezer", appContext.getPackageName());

        System.out.println("Test");
        NotificationHelper.scheduleNotification(appContext, frozenItem, TimeUnit.SECONDS, LocalDateTime.now().plusSeconds(5));
    }
}
