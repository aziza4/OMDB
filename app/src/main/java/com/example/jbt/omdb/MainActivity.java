package com.example.jbt.omdb;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;


public class MainActivity extends AppCompatActivity
        implements EditFragment.OnEditFragListener, FullPosterFragment.OnPosterFragListener {

    public static final String LOG_CAT = "OMDB:";

    private MainFragment mMainFrag;
    private FragmentHelper mFragmentHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Utility.setContentViewWithLocaleChange(this, R.layout.activity_main, R.string.app_name);


    }

    @Override
    protected void onStart() {
        super.onStart();

        boolean isTabletMode = findViewById(R.id.editFragContainer) != null;
        SharedPrefHelper sharedPrefHelper = new SharedPrefHelper(this);
        sharedPrefHelper.saveTabletMode(isTabletMode);

        // replacing fragments works well only from OnStart() and not onCreate() see: http://stackoverflow.com/questions/17229500/oncreateview-in-fragment-is-not-called-immediately-even-after-fragmentmanager
        mFragmentHelper = new FragmentHelper(this, isTabletMode);
        mMainFrag = mFragmentHelper.replaceMainActivityFragments();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode != EditFragment.REQUEST_TAKE_PHOTO)
            return;

        EditFragment editFrag = mFragmentHelper.getEditFragmentIfExists();

        if (editFrag != null)
            editFrag.onCameraActivityResult(resultCode);
    }

    @Override public void onMovieSaved() {
        mMainFrag.onMovieSaved();
    } // refresh main list
    @Override public void onPosterClicked() { mFragmentHelper.replaceToFullPosterFragment(); }
    @Override public void onClose() { mFragmentHelper.replaceEditFragment(true); }
}
