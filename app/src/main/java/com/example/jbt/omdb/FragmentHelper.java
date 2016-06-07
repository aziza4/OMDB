package com.example.jbt.omdb;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.transition.Explode;
import android.transition.Transition;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;


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

        editFrag = CreateEditFragment(movie);
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
                .replace(R.id.editFragContainer, new BlankEditFragment())
                .commit();

        mFragManager.executePendingTransactions();
    }


    public void onPosterClose(Movie movie)
    {
        EditFragment editFrag = CreateEditFragment(movie); // change from poster frag back to edit frag
        replaceToFragment(editFrag, R.id.editFragContainer);
    }


    public void onPosterClick(Movie movie)
    {
        FullPosterFragment posterFrag = CreatePosterFragment(movie);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) { // lollipop and up --> fancy transition

            Fragment editFrag = mFragManager.findFragmentById(R.id.editFragContainer);
            ImageView posterImageView = (ImageView) mActivity.findViewById(R.id.posterImageView);
            String transitionName = mActivity.getString(R.string.poster_shared_element_name);

            PosterTransition enterPosterTransition = new PosterTransition();
            enterPosterTransition.addListener(new Transition.TransitionListener() {
                @Override
                public void onTransitionEnd(Transition transition) {
                    ImageView closeButton = (ImageView) mActivity.findViewById(R.id.closeFullPosterImageView);
                    if (closeButton != null ) closeButton.setVisibility(View.VISIBLE);
                }

                @Override public void onTransitionStart(Transition transition) {}
                @Override public void onTransitionCancel(Transition transition) {}
                @Override public void onTransitionPause(Transition transition) {}
                @Override public void onTransitionResume(Transition transition) {}
            });

            posterFrag.setSharedElementEnterTransition(enterPosterTransition);
            editFrag.setExitTransition(new Explode());
            posterFrag.setSharedElementReturnTransition(new PosterTransition());

            mFragManager
                    .beginTransaction()
                    .addSharedElement(posterImageView, transitionName)
                    .replace(R.id.editFragContainer, posterFrag)
                    .commit();
        }
        else { // lollipop and down --> basic scale animation

            replaceToFragment(posterFrag, R.id.editFragContainer);

            Animation scaleGrowAnimation = AnimationUtils.loadAnimation(mActivity, R.anim.poster_grow);
            scaleGrowAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationEnd(Animation animation) {
                    ImageView closeButton = (ImageView) mActivity.findViewById(R.id.closeFullPosterImageView);
                    if (closeButton != null ) closeButton.setVisibility(View.VISIBLE);
                }

                @Override public void onAnimationStart(Animation animation) {}
                @Override public void onAnimationRepeat(Animation animation) {}
            });

            ImageView posterImageView = (ImageView) mActivity.findViewById(R.id.fullPosterImageView);
            if (posterImageView != null ) posterImageView.startAnimation(scaleGrowAnimation);
        }
    }



    // ---------------------------   Private methods   ----------------------------------

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
