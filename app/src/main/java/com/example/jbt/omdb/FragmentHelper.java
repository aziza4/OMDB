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


    public EditFragment replaceEditActivityFragment() {

        if ( mInTabletMode )
            return null; // on Tablet - containers are handling elsewher Edit fragment

        Fragment frag = mFragManager.findFragmentById(R.id.editFragContainer);

        EditFragment editFragment = new EditFragment();

        if (frag == null)
            replaceToFragment(editFragment, R.id.editFragContainer);

        return editFragment;
    }


    public void replaceContainerFragments(Fragment frag, int containerId) {

        replaceToFragment(frag, containerId);

        if ( ! mInTabletMode )
            return;  // no editFragContainer in phone mode

        Fragment editFrag = mFragManager.findFragmentById(R.id.editFragContainer);

        if (editFrag == null)
            replaceToFragment(new BlankEditFragment(), R.id.editFragContainer);
    }


    private Fragment replaceToFragment(Fragment fragment, int containerId)
    {
        mFragManager.beginTransaction()
                .replace(containerId, fragment)
                .commit();

        mFragManager.executePendingTransactions();

        return fragment;
    }


    public void replaceToFullPosterFragment()
    {
        replaceToFragment(new FullPosterFragment(), R.id.editFragContainer);
    }


    public void replaceBackToEditFragment()
    {
        replaceToEditFragment();
    }

    private EditFragment replaceToEditFragment()
    {
        return (EditFragment)replaceToFragment(new EditFragment(), R.id.editFragContainer);
    }


    public void replaceToEmptyEditFragment() {

        if (!mInTabletMode)
            return;

        EditFragment editFrag =  replaceToEditFragment();
        editFrag.replaceMovie(new Movie(""));
        editFrag.refreshLayout();
    }


    public void launchEditOperation()
    {
        if ( mInTabletMode ) {

            EditFragment editFrag = getEditFragmentIfExists();

            if ( editFrag == null )
                editFrag =  replaceToEditFragment();

            editFrag.refreshLayout();
            return;
        }

        Intent intent = new Intent(mActivity, EditActivity.class);
        mActivity.startActivity(intent);
    }


    public void removeEditFragmentIfExists()
    {
        mFragManager.beginTransaction()
                .replace(R.id.editFragContainer, new BlankEditFragment())
                .commit();

        mFragManager.executePendingTransactions();
    }


    public EditFragment getEditFragmentIfExists()
    {
        Fragment frag = mFragManager.findFragmentById(R.id.editFragContainer);
        return frag instanceof EditFragment ? (EditFragment)frag : null;

    }
}
