package com.example.jbt.omdb;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;


public class MainActivity extends AppCompatActivity {

    public static final String LOG_CAT = "OMDB:";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utility.changeLocale(this);
        setContentView(R.layout.activity_main);
        Utility.resetTitle(this, R.string.app_name); // workaround android bug...
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (Utility.wasLocaleChanged(this))
            recreate(); // language change to take immediately affect
    }
}
