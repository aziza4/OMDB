package com.example.jbt.omdb;


import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class EditActivity extends AppCompatActivity
        implements EditFragment.OnEditFragListener, FullPosterFragment.OnPosterFragListener {

    private EditFragment mEditFrag;
    private FragmentHelper mFragmentHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utility.setContentViewWithLocaleChange(this, R.layout.activity_edit, R.string.edit_name);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // replacing fragments works well only from OnStart() and not onCreate() see: http://stackoverflow.com/questions/17229500/oncreateview-in-fragment-is-not-called-immediately-even-after-fragmentmanager
        mFragmentHelper = new FragmentHelper(this, false); // EditActivity only in 'phone' mode
        mEditFrag = mFragmentHelper.replaceEditActivityFragment();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == EditFragment.REQUEST_TAKE_PHOTO)
            mEditFrag.onCameraActivityResult(resultCode); // save captured in gallery
    }

    @Override public void onMovieSaved() { }
    @Override public void onPosterClicked() { mFragmentHelper.replaceToFullPosterFragment(); }
    @Override public void onClose() { mFragmentHelper.replaceEditFragment(true); }
}
