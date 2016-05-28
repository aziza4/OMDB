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

        if ( mInTabletMode ) // only in phone mode
            return null;

        Fragment frag = mFragManager.findFragmentById(R.id.editFragContainer);

        EditFragment editFragment = new EditFragment();

        if (frag == null || frag instanceof EditFragment) {

            mFragManager.beginTransaction()
                    .replace(R.id.editFragContainer, editFragment)
                    .commit();

            mFragManager.executePendingTransactions();
        }

        replaceEditFragment(false);
        return editFragment;

    }

    public MainFragment replaceMainActivityFragments() {
        MainFragment mainFragment = new MainFragment();
        replaceMainOrSearchFragments(mainFragment, R.id.mainFragContainer);
        return mainFragment;
    }

    public void replaceWebSearchActivityFragments() {
        WebSearchFragment webSearchFragment = new WebSearchFragment();
        replaceMainOrSearchFragments(webSearchFragment, R.id.webSearchFragContainer);
    }

    private void replaceMainOrSearchFragments(Fragment frag, int containerId) {

        mFragManager.beginTransaction()
                .replace(containerId, frag)
                .commit();

        mFragManager.executePendingTransactions();

        if ( ! mInTabletMode ) // no editFragContainer in phone mode
            return;

        replaceEditFragment(false);
    }


    public void replaceEditFragment(boolean changeToEditFrag)
    {
        Fragment frag = mFragManager.findFragmentById(R.id.editFragContainer);

        if (frag == null && !mInTabletMode)
            return; // exit in phone mode - will be handled by EditActivity

        if (frag == null) {
            replaceToBlankEditFragment(); // tablet mode, need to replace with blank fragment
            return;
        }

        if (!changeToEditFrag)
            return; // no need to convert type or force refresh

        if ( frag instanceof BlankEditFragment || frag instanceof FullPosterFragment)
            frag = replaceToEditFragment(); // either for new movie, or close poster operations

        ((EditFragment)frag).refreshLayout(); // frag was replaced, needs to be refreshed.
    }


    private Fragment replaceToFragment(Fragment fragment)
    {
        mFragManager.beginTransaction()
                .replace(R.id.editFragContainer, fragment)
                .commit();

        mFragManager.executePendingTransactions();

        return fragment;
    }

    private void replaceToBlankEditFragment()
    {
        replaceToFragment(new BlankEditFragment());
    }

    private EditFragment replaceToEditFragment()
    {
        return (EditFragment)replaceToFragment(new EditFragment());
    }


    private void replaceToPosterFragment()
    {
        replaceToFragment(new FullPosterFragment());
    }


    public void replaceToFullPosterFragment()
    {
        replaceToPosterFragment();
    }


    public void launchEditOperation()
    {
        if ( mInTabletMode ) {
            replaceEditFragment(true);
            return;
        }

        Intent intent = new Intent(mActivity, EditActivity.class);
        mActivity.startActivity(intent);
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
