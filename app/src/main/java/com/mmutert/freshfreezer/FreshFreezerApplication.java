package com.mmutert.freshfreezer;

import android.app.Application;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import com.mmutert.freshfreezer.util.ThemeHelper;


public class FreshFreezerApplication extends Application {

    public void onCreate() {
        super.onCreate();
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(this);
        String prefNightModeKey = getString(R.string.pref_night_mode_key);
        String nightMode = sharedPreferences.getString(prefNightModeKey, getString(R.string.theme_value_default));
        ThemeHelper.applyTheme(nightMode, this);
    }
}
