package com.example.jbt.omdb;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;


public class MainActivity extends AppCompatActivity {

    public static final String LOG_CAT = "OMDB:";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Utility.setContentViewWithLocaleChange(this, R.layout.activity_main, R.string.app_name);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (Utility.wasLocaleChanged(this))
            recreate(); // language change to take immediately affect
    }
}
