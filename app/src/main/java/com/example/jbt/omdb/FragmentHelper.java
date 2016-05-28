package com.example.jbt.omdb;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;


class FragmentHelper {

    private final boolean mInTabletMode;
    private final AppCompatActivity mActivity;
    private final MoviesDBHelper mDbHelper;
    private final FragmentManager mFragManager;


    public FragmentHelper(Activity activity, boolean inTabletMode)
    {
        mActivity = (AppCompatActivity) activity;
        mInTabletMode = inTabletMode;
        mFragManager = mActivity.getSupportFragmentManager();
        mDbHelper = new MoviesDBHelper(activity);
    }


    public MainFragment replaceMainFragment()
    {
        MainFragment mainFragment = new MainFragment();

        mFragManager.beginTransaction()
                .replace(R.id.mainFragContainer, mainFragment)
                .commit();

        if (mInTabletMode) // no editFragContainer in phone mode
            mFragManager.beginTransaction()
                    .replace(R.id.editFragContainer, new BlankEditFragment())
                    .commit();

        mFragManager.executePendingTransactions();

        return mainFragment;
    }

    public WebSearchFragment replaceWebSearchFragment()
    {
        WebSearchFragment webSearchFragment = new WebSearchFragment();

        mFragManager.beginTransaction()
                .replace(R.id.webSearchFragContainer, webSearchFragment)
                .commit();

        if (mInTabletMode) // no editFragContainer in phone mode
            mFragManager.beginTransaction()
                    .replace(R.id.editFragContainer, new BlankEditFragment())
                    .commit();

        mFragManager.executePendingTransactions();

        return webSearchFragment;
    }



    public void replaceMovieOnEditFragment(Movie movie)
    {
        mDbHelper.updateOrInsertEditMovie(movie);

        if (! mInTabletMode)
            return;  // on phone mode, EditActivity will take care of EditFragment

        EditFragment editFrag = (EditFragment)mFragManager.findFragmentById(R.id.editFragContainer);

        if (editFrag == null)
            editFrag = replaceEditFragment();

        editFrag.refreshLayout();
    }


    public EditFragment replaceEditFragment()
    {
        EditFragment editFragment = new EditFragment();

        mFragManager.beginTransaction()
                .replace(R.id.editFragContainer, editFragment)
                .commit();

        mFragManager.executePendingTransactions();

        return editFragment;
    }


    public void launchEditOperation(Movie movie)
    {
        if ( mInTabletMode ) {

            replaceEditFragment();

        } else {

            Intent intent = new Intent(mActivity, EditActivity.class);
            mActivity.startActivity(intent);
        }

        replaceMovieOnEditFragment(movie);
    }


    public void removeEditFragmentIfExists()
    {
        Fragment editFrag = mFragManager.findFragmentById(R.id.editFragContainer);

        if (editFrag == null)
            return;

        mFragManager.beginTransaction()
                .replace(R.id.editFragContainer, new BlankEditFragment())
                .commit();

        mFragManager.executePendingTransactions();
    }


    public EditFragment getEditFragmentIfExists()
    {
        return  (EditFragment) mFragManager.findFragmentById(R.id.editFragContainer);
    }
}
