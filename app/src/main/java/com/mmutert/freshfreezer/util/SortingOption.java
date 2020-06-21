package com.mmutert.freshfreezer.util;

public enum SortingOption {

    DATE_ADDED,
    DATE_FROZEN_AT,
    DATE_BEST_BEFORE,
    NAME;

    public enum SortingOrder {
        ASCENDING, DESCENDING
    }
}
