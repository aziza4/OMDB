package com.example.jbt.omdb;

import android.app.Activity;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;


class FragmentHelper {

    private final boolean mInTabletMode;
    private final MoviesDBHelper mDbHelper;
    private final FragmentManager mFragManager;


    public FragmentHelper(Activity activity, boolean inTabletMode)
    {
        AppCompatActivity mActivity = (AppCompatActivity) activity;
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

        mFragManager.executePendingTransactions();

        return mainFragment;
    }

    public void replaceMovieOnEditFragment(Movie movie)
    {
        mDbHelper.updateOrInsertEditMovie(movie);

        if (! mInTabletMode)
            return;  // in "phone" mode, EditActivity will take care of EditFragment

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


    public void removeEditFragmentIfExists()
    {
        EditFragment editFrag = (EditFragment) mFragManager.findFragmentById(R.id.editFragContainer);

        if (editFrag == null)
            return;

        mFragManager.beginTransaction()
                .remove(editFrag)
                .commit();

        mFragManager.executePendingTransactions();
    }


    public EditFragment getEditFragmentIfExists()
    {
        return  (EditFragment) mFragManager.findFragmentById(R.id.editFragContainer);
    }
}
