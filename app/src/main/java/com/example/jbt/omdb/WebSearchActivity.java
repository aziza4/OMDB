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

    }

    @Override
    protected void onStart() {
        super.onStart();

        boolean isTabletMode = findViewById(R.id.editFragContainer) != null;
        SharedPrefHelper sharedPrefHelper = new SharedPrefHelper(this);
        sharedPrefHelper.saveTabletMode(isTabletMode);

        // replacing fragments works well only from OnStart() and not onCreate() see: http://stackoverflow.com/questions/17229500/oncreateview-in-fragment-is-not-called-immediately-even-after-fragmentmanager
        mFragmentHelper = new FragmentHelper(this, isTabletMode);
        mFragmentHelper.replaceContainerFragments(new WebSearchFragment(), R.id.webSearchFragContainer);
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

    @Override public void onMovieSaved() { }
    @Override public void onPosterClicked() { mFragmentHelper.replaceToFullPosterFragment(); }
    @Override public void onClose() { mFragmentHelper.replaceBackToEditFragment(); }
}
