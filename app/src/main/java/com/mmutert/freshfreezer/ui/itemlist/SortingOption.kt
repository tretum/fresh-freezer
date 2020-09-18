package com.mmutert.freshfreezer.ui.itemlist

enum class SortingOption {
    DATE_CHANGED, DATE_ADDED, DATE_FROZEN_AT, DATE_BEST_BEFORE, NAME;

    enum class SortingOrder {
        ASCENDING, DESCENDING
    }
}