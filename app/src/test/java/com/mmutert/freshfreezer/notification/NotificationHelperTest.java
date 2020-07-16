package com.mmutert.freshfreezer.notification;

import com.mmutert.freshfreezer.notification.NotificationHelper;

import org.joda.time.DateTime;
import org.junit.Test;

import org.joda.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class NotificationHelperTest {

    @Test
    public void testCalculateOffset() {

        LocalDateTime startDate = new LocalDateTime(2020, 7, 15, 14, 0);
        LocalDateTime goalDate = new LocalDateTime(2020, 7, 16, 14, 0);

        long offset = NotificationHelper.calculateOffset(TimeUnit.HOURS, startDate, goalDate);
        assertTrue(offset >= 23);
        assertTrue(offset <= 25);
        assertEquals("Calculated offset does not match actual offset", 24, offset);

        offset = NotificationHelper.calculateOffset(TimeUnit.SECONDS, startDate, goalDate);
        assertEquals("Calculated offset does not match actual offset", 86400, offset);
    }
}
