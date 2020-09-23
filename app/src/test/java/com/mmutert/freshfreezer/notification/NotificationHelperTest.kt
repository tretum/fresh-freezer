package com.mmutert.freshfreezer.notification

import com.mmutert.freshfreezer.notification.NotificationHelper.calculateOffset
import org.joda.time.LocalDateTime
import org.junit.Assert
import org.junit.Test
import java.util.concurrent.TimeUnit

class NotificationHelperTest {
    @Test
    fun testCalculateOffset() {
        val startDate = LocalDateTime(2020, 7, 15, 14, 0)
        val goalDate = LocalDateTime(2020, 7, 16, 14, 0)
        var offset = calculateOffset(TimeUnit.HOURS, startDate, goalDate)
        Assert.assertTrue(offset >= 23)
        Assert.assertTrue(offset <= 25)
        Assert.assertEquals("Calculated offset does not match actual offset", 24, offset)
        offset = calculateOffset(TimeUnit.SECONDS, startDate, goalDate)
        Assert.assertEquals("Calculated offset does not match actual offset", 86400, offset)
    }
}