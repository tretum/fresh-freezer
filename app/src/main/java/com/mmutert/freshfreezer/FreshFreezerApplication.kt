package com.mmutert.freshfreezer

import android.app.Application
import androidx.preference.PreferenceManager
import com.mmutert.freshfreezer.util.ThemeHelper

class FreshFreezerApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val prefNightModeKey = getString(R.string.pref_night_mode_key)
        val nightMode = sharedPreferences.getString(prefNightModeKey, getString(R.string.theme_value_default))
        ThemeHelper.applyTheme(nightMode!!, this)
    }
}