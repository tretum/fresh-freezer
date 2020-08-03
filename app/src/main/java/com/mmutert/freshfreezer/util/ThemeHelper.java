package com.mmutert.freshfreezer.util;

import android.content.Context;
import android.os.Build;

import androidx.appcompat.app.AppCompatDelegate;

import com.mmutert.freshfreezer.R;


public class ThemeHelper {

    public static void applyTheme(String preferenceValue, Context context) {

        String lightModeKey = context.getString(R.string.theme_value_light);
        String darkModeKey = context.getString(R.string.theme_value_dark);

        if (lightModeKey.equals(preferenceValue)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        } else if (darkModeKey.equals(preferenceValue)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY);
        }
    }
}
