package com.example.jbt.omdb;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;


class FragmentHelper {

    private final boolean mInTabletMode;
    private final AppCompatActivity mActivity;
    private final FragmentManager mFragManager;

    public FragmentHelper(Activity activity, boolean inTabletMode) {
        mActivity = (AppCompatActivity) activity;
        mInTabletMode = inTabletMode;
        mFragManager = mActivity.getSupportFragmentManager();
    }


    public void replaceEditActivityFragment(Movie movie) // phone mode only
    {
        EditFragment editFrag = CreateEditFragment(movie);
        replaceToFragment(editFrag, R.id.editFragContainer);
    }

    public void replaceContainerFragments(Fragment frag, int containerId) {

        replaceToFragment(frag, containerId);

        if ( ! mInTabletMode )
            return;  // no editFragContainer in phone mode

        Fragment editFrag = mFragManager.findFragmentById(R.id.editFragContainer);

        if (editFrag == null)
            replaceToFragment(new BlankEditFragment(), R.id.editFragContainer);
    }


    public void onMovieEdit(Movie movie)
    {
        if ( mInTabletMode )
        {
            EditFragment editFrag = CreateEditFragment(movie);
            replaceToFragment(editFrag, R.id.editFragContainer);
            return;
        }

        Intent intent = new Intent(mActivity, EditActivity.class);
        intent.putExtra(WebSearchActivity.INTENT_MOVIE_KEY, movie);
        mActivity.startActivity(intent);
    }


    public void onMovieDelete()
    {
        if (!mInTabletMode)
            return;

        mFragManager.beginTransaction()
                .replace(R.id.editFragContainer, new BlankEditFragment())
                .commit();

        mFragManager.executePendingTransactions();
    }


    public void onPosterClose(Movie movie)
    {
        EditFragment editFrag = CreateEditFragment(movie);
        replaceToFragment(editFrag, R.id.editFragContainer);
    }


    public void onPosterClick(Movie movie)
    {
        FullPosterFragment posterFrag = CreatePosterFragment(movie);
        replaceToFragment(posterFrag, R.id.editFragContainer);
    }


    public void OnPhotoTakenActivityResult(int requestCode, int resultCode)
    {
        if (requestCode != EditFragment.REQUEST_TAKE_PHOTO)
            return;

        EditFragment editFrag = getEditFragment();

        if (editFrag != null)
            editFrag.onCameraActivityResult(resultCode);
    }

    private EditFragment getEditFragment()
    {
        Fragment frag = mFragManager.findFragmentById(R.id.editFragContainer);

        if (frag == null || !(frag instanceof EditFragment))
            return null;

        return (EditFragment)frag;
    }


    private void replaceToFragment(Fragment fragment, int containerId)
    {
        mFragManager.beginTransaction()
                .replace(containerId, fragment)
                .commit();

        mFragManager.executePendingTransactions();
    }


    private EditFragment CreateEditFragment(Movie movie)
    {
        EditFragment editFrag = new EditFragment();
        editFrag.setMovie(movie);
        return editFrag;
    }

    private FullPosterFragment CreatePosterFragment(Movie movie)
    {
        FullPosterFragment posterFrag = new FullPosterFragment();
        posterFrag.setMovie(movie);
        return posterFrag;
    }
}
