package com.example.jbt.omdb;


import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;


public class SettingsFragment extends PreferenceFragment
        implements Preference.OnPreferenceChangeListener{


    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Utility.changeLocale(getActivity());
        addPreferencesFromResource(R.xml.pref_general);
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_sort_key)));
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_lang_key)));
    }


    private void bindPreferenceSummaryToValue(Preference preference) {
        preference.setOnPreferenceChangeListener(this);

        onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }


    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {

        String valueStr = newValue.toString();

        if ( preference instanceof ListPreference)
        {

            ListPreference listPreference = (ListPreference) preference;
            int index = listPreference.findIndexOfValue(valueStr);

            if (index >= 0)
                preference.setSummary(listPreference.getEntries()[index]);

        } else {

            preference.setSummary(valueStr);
        }

        return true;
    }
}
