package com.example.jbt.omdb;


import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class EditActivity extends AppCompatActivity {

    private Movie mMovie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utility.setContentViewWithLocaleChange(this, R.layout.activity_edit, R.string.edit_name);

        Intent intent = getIntent();
        mMovie = intent.getParcelableExtra(WebSearchActivity.INTENT_MOVIE_KEY);

        EditFragment editFrag = new EditFragment();
        FragmentManager manager = getSupportFragmentManager();

        manager.beginTransaction()
                .replace(R.id.editFragContainer, editFrag)
                .commit();

        editFrag.setMovie(mMovie);
    }
}
