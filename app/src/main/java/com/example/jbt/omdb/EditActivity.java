package com.example.jbt.omdb;


import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class EditActivity extends AppCompatActivity implements EditFragment.OnEditDoneListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utility.setContentViewWithLocaleChange(this, R.layout.activity_edit, R.string.edit_name);

        FragmentManager manager = getSupportFragmentManager();

        EditFragment editFrag = new EditFragment();

        manager.beginTransaction()
                .replace(R.id.editFragContainer, editFrag)
                .commit();

        editFrag.setTabletMode(false);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data); // ensures frag onActivityResult() to get called
    }

    @Override
    public void onMovieSaved() {}

}
