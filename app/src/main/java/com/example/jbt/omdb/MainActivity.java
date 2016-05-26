package com.example.jbt.omdb;

import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;


public class MainActivity extends AppCompatActivity implements EditFragment.OnEditDoneListener {

    public static final String LOG_CAT = "OMDB:";

    private MainFragment mMainFrag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Utility.setContentViewWithLocaleChange(this, R.layout.activity_main, R.string.app_name);

        boolean isTabletMode = false;
        mMainFrag = new MainFragment();
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();

        transaction.replace(R.id.mainFragContainer, mMainFrag);

        if (findViewById(R.id.editFragContainer) != null) {
            isTabletMode = true;
        }

        transaction.commit();
        mMainFrag.setTabletMode(isTabletMode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data); // ensures frag onActivityResult() to get called
    }

    @Override
    public void onMovieSaved() {
        mMainFrag.onMovieSaved();
    }
}
