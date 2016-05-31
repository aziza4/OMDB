package com.example.jbt.omdb;


import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class EditActivity extends AppCompatActivity
        implements MainFragment.OnMainFragListener, EditFragment.OnEditFragListener,
        FullPosterFragment.OnPosterFragListener {

    private FragmentHelper mFragmentHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utility.setContentViewWithLocaleChange(this, R.layout.activity_edit, R.string.edit_name);
    }

    @Override
    protected void onStart() {
        super.onStart();

        Intent intent = getIntent();
        Movie movie = intent.getParcelableExtra(WebSearchActivity.INTENT_MOVIE_KEY);

        // replacing fragments works well only from OnStart() and not onCreate() see: http://stackoverflow.com/questions/17229500/oncreateview-in-fragment-is-not-called-immediately-even-after-fragmentmanager
        mFragmentHelper = new FragmentHelper(this, false); // EditActivity only in 'phone' mode
        mFragmentHelper.addEditActivityFragment(movie);
    }

    @Override public void onMovieSaved() { }
    @Override public void onPosterClicked(Movie movie) { mFragmentHelper.onPosterClick(movie); }
    @Override public void onPosterClose(Movie movie) { mFragmentHelper.onPosterClose(movie); }
    @Override public void onMovieEdit(Movie movie) { mFragmentHelper.onMovieEdit(movie); }
}
