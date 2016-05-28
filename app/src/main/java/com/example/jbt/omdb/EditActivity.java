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

        mFragmentHelper = new FragmentHelper(this, false); // EditActivity only in 'phone' mode
        mEditFrag = mFragmentHelper.replaceEditFragment();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == EditFragment.REQUEST_TAKE_PHOTO && resultCode == Activity.RESULT_OK)
            mEditFrag.onCameraActivityResult(); // save captured in gallery
    }

    @Override public void onMovieSaved() { }
    @Override public void onPosterClicked() { mFragmentHelper.replaceToFullPosterFragment(); }
    @Override public void onClose() { mFragmentHelper.replaceEditFragment(); }
}
