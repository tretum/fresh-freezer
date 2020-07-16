package com.mmutert.freshfreezer.data;

public class PendingNotification {
    private int offsetAmount;
    private TimeOffsetUnit timeUnit;

    public PendingNotification(final int offsetAmount, final TimeOffsetUnit timeUnit) {
        this.offsetAmount  = offsetAmount;
        this.timeUnit      = timeUnit;
    }

    public int getOffsetAmount() {
        return offsetAmount;
    }

    public TimeOffsetUnit getTimeUnit() {
        return timeUnit;
    }
}
