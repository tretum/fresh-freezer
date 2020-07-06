package com.mmutert.freshfreezer.ui;

import androidx.annotation.NonNull;


class PendingNotification {
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

    enum OffsetUnit {
        DAYS("days"), WEEKS("weeks"), MONTHS("months");

        private String name;

        OffsetUnit(String name) {
            this.name = name;
        }

        @NonNull
        @Override
        public String toString() {
            return name;
        }
    }
}
