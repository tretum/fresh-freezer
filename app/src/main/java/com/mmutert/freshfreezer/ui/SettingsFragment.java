package com.mmutert.freshfreezer.ui;

import android.os.Bundle;

import androidx.preference.ListPreference;
import androidx.preference.PreferenceFragmentCompat;

import com.mmutert.freshfreezer.R;
import com.mmutert.freshfreezer.util.ThemeHelper;


public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

        setPreferencesFromResource(R.xml.root_preferences, rootKey);

        ListPreference nightModePref = findPreference(getString(R.string.pref_night_mode_key));

        if (nightModePref != null) {
            nightModePref.setOnPreferenceChangeListener(
                    (preference, newValue) -> {
                        String themeOption = (String) newValue;
                        ThemeHelper.applyTheme(themeOption, getActivity());
                        return true;
                    });
        }
    }


}