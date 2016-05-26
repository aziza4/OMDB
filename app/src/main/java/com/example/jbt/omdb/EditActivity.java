package com.example.jbt.omdb;


import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class EditActivity extends AppCompatActivity implements EditFragment.OnEditDoneListener{

    public static final int REQUEST_TAKE_PHOTO = 1;
    private EditFragment mEditFrag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utility.setContentViewWithLocaleChange(this, R.layout.activity_edit, R.string.edit_name);

        FragmentManager manager = getSupportFragmentManager();

        mEditFrag = new EditFragment();

        manager.beginTransaction()
                .replace(R.id.editFragContainer, mEditFrag)
                .commit();

        manager.executePendingTransactions();

        mEditFrag.setTabletMode(false);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data); // ensures frag onActivityResult() to get called

        if (requestCode != REQUEST_TAKE_PHOTO|| resultCode != Activity.RESULT_OK)
            return;

        mEditFrag.getUrlAndSaveImageToGallery();
    }


    @Override
    public void onMovieSaved() {}
}
