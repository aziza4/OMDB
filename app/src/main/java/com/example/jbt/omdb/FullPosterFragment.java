package com.example.jbt.omdb;



import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;


public class FullPosterFragment extends Fragment {


    private Movie mMovie;
    private OnPosterFragListener mListener;
    private ImageView mPosterImageView;
    private ImageView mCloseButton;
    private Animation mScaleGrowAnimation;
    private Animation mScaleShrinkAnimation;

    public FullPosterFragment() { }
    public void setMovie(Movie movie) { mMovie = movie; }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mListener = (OnPosterFragListener) context;
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

        mPosterImageView = (ImageView) view.findViewById(R.id.fullPosterImageView);
        mPosterImageView.setImageBitmap(mMovie.getImage());

        mCloseButton = (ImageView) view.findViewById(R.id.closeFullPosterImageView);

        mCloseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCloseButton.setVisibility(View.INVISIBLE);
                mPosterImageView.startAnimation(mScaleShrinkAnimation);
            }
        });

        mScaleGrowAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.poster_grow);
        mScaleGrowAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                mCloseButton.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mCloseButton.setVisibility(View.VISIBLE);
            }

            @Override public void onAnimationRepeat(Animation animation) {}
        });


        mScaleShrinkAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.poster_shrink);
        mScaleShrinkAnimation.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationEnd(Animation animation) {
                mListener.onPosterClose(mMovie);
            }

            @Override public void onAnimationStart(Animation animation) {}
            @Override public void onAnimationRepeat(Animation animation) {}
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        mPosterImageView.startAnimation(mScaleGrowAnimation);
    }

    public interface OnPosterFragListener {
        void onPosterClose(Movie movie);
    }
}
