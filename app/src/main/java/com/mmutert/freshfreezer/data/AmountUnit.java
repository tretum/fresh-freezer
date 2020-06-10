package com.mmutert.freshfreezer.data;

import androidx.annotation.NonNull;

public enum AmountUnit {
    GRAMS("g"), KILOGRAMS("kg"), PIECES("pcs");

    private String name;

    AmountUnit(String name) {
        this.name = name;
    }

    @NonNull
    @Override
    public String toString() {
        return name;
    }
}
