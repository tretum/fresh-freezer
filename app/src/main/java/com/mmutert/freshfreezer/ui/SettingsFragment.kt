package com.mmutert.freshfreezer.ui

import android.os.Bundle
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.mmutert.freshfreezer.R
import com.mmutert.freshfreezer.util.ThemeHelper

class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle, rootKey: String) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
        val nightModePref = findPreference<ListPreference>(getString(R.string.pref_night_mode_key))
        if (nightModePref != null) {
            nightModePref.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { preference: Preference?, newValue: Any? ->
                val themeOption = newValue as String?
                ThemeHelper.applyTheme(themeOption, activity)
                true
            }
        }
    }
}