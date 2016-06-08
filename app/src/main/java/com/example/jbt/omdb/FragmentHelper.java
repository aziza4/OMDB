package com.example.jbt.omdb;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
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


    public void addEditActivityFragment(Movie movie) // phone mode only
    {
        Fragment editFrag = mFragManager.findFragmentById(R.id.editFragContainer);

        if (editFrag != null )
            return; // do not replace in case of device rotation

        editFrag = EditFragment.newInstance(movie);
        replaceToFragment(editFrag, R.id.editFragContainer);
    }


    public void replaceContainerFragments(Fragment frag, int containerId) {

        replaceToFragment(frag, containerId);

        if ( ! mInTabletMode )
            return;  // no editFragContainer in phone mode

        Fragment editFrag = mFragManager.findFragmentById(R.id.editFragContainer);

        if (editFrag == null)
            replaceToFragment(BlankEditFragment.newInstance(), R.id.editFragContainer);
    }


    public void onMovieEdit(Movie movie)
    {
        if ( mInTabletMode )
        {
            EditFragment editFrag = EditFragment.newInstance(movie);
            replaceToFragment(editFrag, R.id.editFragContainer); // movie may be different - always replace fragment
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
                .replace(R.id.editFragContainer, BlankEditFragment.newInstance())
                .commit();
    }


    public void onPosterClose(Movie movie)
    {
        EditFragment editFrag = EditFragment.newInstance(movie); // change from poster frag back to edit frag
        replaceToFragment(editFrag, R.id.editFragContainer);
    }


    public void onPosterClick(Movie movie)
    {
        FullPosterFragment posterFrag = FullPosterFragment.newInstance(movie); // create instance and set transition

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) { // lollipop and up --> fancy transition

            mFragManager
                    .beginTransaction()
                    .addSharedElement(
                            mActivity.findViewById(R.id.posterImageView),
                            mActivity.getString(R.string.poster_shared_element_name))
                    .replace(R.id.editFragContainer, posterFrag)
                    .commit();
        }
        else { // lollipop and down --> basic scale animation
            replaceToFragment(posterFrag, R.id.editFragContainer);
        }
    }



    // ---------------------------   Private methods   ----------------------------------

    private void replaceToFragment(Fragment fragment, int containerId)
    {
        mFragManager.beginTransaction()
                .replace(containerId, fragment)
                .commit();
    }
}
