package com.example.jbt.omdb;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;


public class MainActivity extends AppCompatActivity {

    public static final String LOG_CAT = "OMDB:";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Utility.setContentViewWithLocaleChange(this, R.layout.activity_main, R.string.app_name);

        boolean isTabletMode = false;
        MainFragment mainFrag = new MainFragment();
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();

        transaction.replace(R.id.mainFragContainer, mainFrag);

        if (findViewById(R.id.editFragContainer) != null) {
            isTabletMode = true;
            //transaction.add(R.id.editFragContainer, new EditFragment());
        }

        transaction.commit();
        mainFrag.setTabletMode(isTabletMode);
    }
}
