package com.example.jbt.omdb;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;


public class MainActivity extends AppCompatActivity implements EditFragment.OnEditDoneListener {

    public static final String LOG_CAT = "OMDB:";

    private MainFragment mMainFrag;
    private FragmentHelper mFragmentHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Utility.setContentViewWithLocaleChange(this, R.layout.activity_main, R.string.app_name);

        boolean isTabletMode = findViewById(R.id.editFragContainer) != null;
        SharedPrefHelper sharedPrefHelper = new SharedPrefHelper(this);
        sharedPrefHelper.saveTabletMode(isTabletMode);

        mFragmentHelper = new FragmentHelper(this, isTabletMode);
        mMainFrag = mFragmentHelper.replaceMainFragment(); // create main fragment
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode != EditFragment.REQUEST_TAKE_PHOTO || resultCode != Activity.RESULT_OK)
            return;

        EditFragment editFrag = mFragmentHelper.getEditFragmentIfExists();

        if (editFrag != null)
            editFrag.onCameraActivityResult();
    }

    @Override
    public void onMovieSaved() {
        mMainFrag.onMovieSaved();
    } // refresh main list
}
