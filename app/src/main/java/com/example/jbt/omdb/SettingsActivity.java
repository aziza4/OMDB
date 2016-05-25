package com.example.jbt.omdb;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;


public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Utility.setContentViewWithLocaleChange(this, R.layout.activity_settings, R.string.settings_name);

        getFragmentManager()
                .beginTransaction()
                .add(R.id.container, new SettingsFragment())
                .commit();
    }
}
