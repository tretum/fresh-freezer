package com.mmutert.freshfreezer.ui.databinding;

import androidx.databinding.InverseMethod;


public class IntStringConverter {

    @InverseMethod("stringToInt")
    public static String intToString(int toConvert) {
        return String.valueOf(toConvert);
    }

    public static int stringToInt(String toConvert) {
        try {
            return Integer.parseInt(toConvert);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
