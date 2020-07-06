package com.mmutert.freshfreezer.ui.databinding;

import androidx.databinding.InverseMethod;

import java.util.Locale;


public class AmountBindingConverter {

    @InverseMethod("stringToFloat")
    public static String floatToString(float newValue) {
        return String.valueOf(newValue);
    }

    public static float stringToFloat(String newValue) {
        try {
            return Float.parseFloat(newValue);
        } catch (NumberFormatException e) {
            return 0.0F;
        }
    }
}
