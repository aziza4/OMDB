package com.example.jbt.omdb;



import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;


public class FullPosterFragment extends Fragment {

    private Movie mMovie;
    private OnPosterFragListener mListener;
    private ImageView mCloseButton;


    public static FullPosterFragment newInstance(Movie movie)
    {
        final FullPosterFragment fragment = new FullPosterFragment();
        fragment.mMovie = movie;
        return fragment;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        mListener = (OnPosterFragListener) context;
        SetTransition(context);
    }


    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {

        if (mMovie != null)
            outState.putParcelable(WebSearchActivity.INTENT_MOVIE_KEY, mMovie);

        super.onSaveInstanceState(outState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_full_poster, container, false);

        if (mMovie == null && savedInstanceState != null)
            mMovie = savedInstanceState.getParcelable(WebSearchActivity.INTENT_MOVIE_KEY);

        Utility.setEditFragBackgroundColor(getActivity(), view);

        return view;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageView posterImageView = (ImageView) view.findViewById(R.id.fullPosterImageView);
        posterImageView.setImageBitmap(mMovie.getImage());

        mCloseButton = (ImageView) view.findViewById(R.id.closeFullPosterImageView);
        mCloseButton.setVisibility(View.INVISIBLE); // later, on trantion/animation finish it will be visible

        mCloseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
mListener.onPosterClose(mMovie);
            }
        });

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {

            Animation scaleGrowAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.poster_grow);
            scaleGrowAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationEnd(Animation animation) {
                    mCloseButton.setVisibility(View.VISIBLE);
                }

                @Override public void onAnimationStart(Animation animation) {}
                @Override public void onAnimationRepeat(Animation animation) {}
            });

            posterImageView.startAnimation(scaleGrowAnimation);
        }
    }


    public interface OnPosterFragListener {
        void onPosterClose(Movie movie);
    }


    private void SetTransition(Context context)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            Transition transition = TransitionInflater
                    .from(context)
                    .inflateTransition(R.transition.poster_transition);

            transition.addListener(new Transition.TransitionListener() {

                @Override
                public void onTransitionEnd(Transition transition) {
                    mCloseButton.setVisibility(View.VISIBLE);
                }

                @Override public void onTransitionStart(Transition transition) {}
                @Override public void onTransitionCancel(Transition transition) {}
                @Override public void onTransitionPause(Transition transition) {}
                @Override public void onTransitionResume(Transition transition) {}
            });

            setSharedElementEnterTransition(transition);
        }
    }
}
