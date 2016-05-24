package com.example.jbt.omdb;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;


public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utility.changeLocale(this);
        setContentView(R.layout.activity_settings);
        Utility.resetTitle(this, R.string.settings_name); // workaround android bug...

        getFragmentManager()
                .beginTransaction()
                .add(R.id.container, new SettingsFragment())
                .commit();
    }
}
