package com.mmutert.freshfreezer.notification

import android.content.Context
import android.util.Log
import androidx.test.core.app.ApplicationProvider
import androidx.work.Configuration
import androidx.work.WorkManager
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.TestListenableWorkerBuilder
import com.mmutert.freshfreezer.data.*
import com.mmutert.freshfreezer.notification.NotificationHelper.createInputDataForItem
import com.mmutert.freshfreezer.util.TimeHelper.currentDateLocalized
import com.mmutert.freshfreezer.util.TimeHelper.currentDateTimeLocalized
import org.junit.Before
import org.junit.Test

class NotificationTest {
    private var context: Context? = null
    private var workManager: WorkManager? = null

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        val config = Configuration.Builder() // Set log level to Log.DEBUG to
            // make it easier to see why tests failed
            .setMinimumLoggingLevel(Log.DEBUG) // Use a SynchronousExecutor to make it easier to write tests
            .setExecutor(SynchronousExecutor())
            .build()

        // Initialize WorkManager for instrumentation tests.
        workManager = WorkManager.getInstance(context!!)
    }

    @Test
    fun testNotificationDisplay() {
        val item = createTestItem()
        val testItemNotification = createTestItemNotification()
        val inputData = createInputDataForItem(context!!, item, testItemNotification)
        val builder = TestListenableWorkerBuilder.from(
            context!!, NotificationWorker::class.java)
        val worker = builder.setInputData(inputData)
            .build()
        val result = worker.startWork().get()
    }

    private fun createTestItem(): StorageItem {
        return StorageItem(
            id = 1,
            name = "NotificationTestItem",
            amount = 10.532f,
            unit = AmountUnit.LITERS,
            bestBeforeDate = currentDateLocalized.plusDays(2),
            itemCreationDate = currentDateTimeLocalized,
            lastChangedAtDate = currentDateTimeLocalized,
            condition = Condition.CHILLED,
        )
    }

    private fun createTestItemNotification(): ItemNotification {
        return ItemNotification(0, null, 1, TimeOffsetUnit.DAYS, 1)
    }
}