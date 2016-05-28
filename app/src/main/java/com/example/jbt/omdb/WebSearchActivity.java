package com.example.jbt.omdb;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class WebSearchActivity extends AppCompatActivity
        implements EditFragment.OnEditFragListener, FullPosterFragment.OnPosterFragListener  {

    private FragmentHelper mFragmentHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utility.setContentViewWithLocaleChange(this, R.layout.activity_web_search, R.string.web_search_name);

        boolean isTabletMode = findViewById(R.id.editFragContainer) != null;
        SharedPrefHelper sharedPrefHelper = new SharedPrefHelper(this);
        sharedPrefHelper.saveTabletMode(isTabletMode);

        mFragmentHelper = new FragmentHelper(this, isTabletMode);
        mFragmentHelper.replaceWebSearchFragment(); // create webSearch fragment
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

    @Override public void onMovieSaved() { }
    @Override public void onPosterClicked() { mFragmentHelper.replaceToFullPosterFragment(); }
    @Override public void onClose() { mFragmentHelper.replaceEditFragment(); }
}
