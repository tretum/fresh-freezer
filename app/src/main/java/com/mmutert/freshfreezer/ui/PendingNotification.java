package com.mmutert.freshfreezer.ui;

public class PendingNotification {
    private int offsetAmount;
    private OffsetUnit timeUnit;

    public PendingNotification(final int offsetAmount, final OffsetUnit timeUnit) {
        this.offsetAmount  = offsetAmount;
        this.timeUnit      = timeUnit;
    }

    public PendingNotification() {

    }

    public void setOffsetAmount(final int offsetAmount) {
        this.offsetAmount = offsetAmount;
    }

    public void setTimeUnit(final OffsetUnit timeUnit) {
        this.timeUnit = timeUnit;
    }

    public int getOffsetAmount() {
        return offsetAmount;
    }

    public OffsetUnit getTimeUnit() {
        return timeUnit;
    }

    public enum OffsetUnit {
        DAYS, WEEKS, MONTHS
    }
}
