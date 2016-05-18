package com.example.jbt.omdb;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;


@SuppressWarnings("deprecation")
public class SettingsActivity extends PreferenceActivity
        implements Preference.OnPreferenceChangeListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.pref_general);
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_sort_key)));
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

        if (! (preference instanceof ListPreference) )
            return true;

        String valueStr = newValue.toString();
        ListPreference listPreference = (ListPreference) preference;
        int index = listPreference.findIndexOfValue(valueStr);

        if (index >= 0)
            preference.setSummary(listPreference.getEntries()[index]);

        return true;
    }


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public Intent getParentActivityIntent() {
        Intent intent = super.getParentActivityIntent();
        return intent != null ? intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP) : null;
    }
}
