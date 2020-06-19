package com.mmutert.freshfreezer.ui;

import androidx.annotation.NonNull;


class PendingNotification {
    private int offsetAmount;
    private OffsetAmount timeUnit;

    public PendingNotification(final int offsetAmount, final OffsetAmount timeUnit) {
        this.offsetAmount  = offsetAmount;
        this.timeUnit      = timeUnit;
    }

    public PendingNotification() {

    }

    public void setOffsetAmount(final int offsetAmount) {
        this.offsetAmount = offsetAmount;
    }

    public void setTimeUnit(final OffsetAmount timeUnit) {
        this.timeUnit = timeUnit;
    }

    public int getOffsetAmount() {
        return offsetAmount;
    }

    public OffsetAmount getTimeUnit() {
        return timeUnit;
    }

    enum OffsetAmount {
        DAYS("days"), WEEKS("weeks"), MONTHS("months");

        private String name;

        OffsetAmount(String name) {
            this.name = name;
        }

        @NonNull
        @Override
        public String toString() {
            return name;
        }
    }
}
