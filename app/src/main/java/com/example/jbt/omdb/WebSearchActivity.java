package com.example.jbt.omdb;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class WebSearchActivity extends AppCompatActivity
        implements WebSearchFragment.OnWebSearchFragListener, EditFragment.OnEditFragListener,
        FullPosterFragment.OnPosterFragListener  {

    public static final String INTENT_MOVIE_KEY = "movie";

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

        mFragmentHelper.OnPhotoTakenActivityResult(requestCode, resultCode);
    }

    @Override public void onMovieSaved() { }
    @Override public void onPosterClicked(Movie movie) { mFragmentHelper.onPosterClick(movie); }
    @Override public void onPosterClose(Movie movie) { mFragmentHelper.onPosterClose(movie); }
    @Override public void onMovieEdit(Movie movie) {  mFragmentHelper.onMovieEdit(movie); }
}
