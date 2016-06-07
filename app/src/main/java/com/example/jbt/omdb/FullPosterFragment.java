package com.example.jbt.omdb;



import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;


public class FullPosterFragment extends Fragment {

    private Movie mMovie;
    private OnPosterFragListener mListener;

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

        return view;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageView posterImageView = (ImageView) view.findViewById(R.id.fullPosterImageView);
        posterImageView.setImageBitmap(mMovie.getImage());

        ImageView closeButton = (ImageView) view.findViewById(R.id.closeFullPosterImageView);
        closeButton.setVisibility(View.INVISIBLE); // later, on trantion/animation finish it will be visible

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onPosterClose(mMovie);
            }
        });
    }


    public interface OnPosterFragListener {
        void onPosterClose(Movie movie);
    }
}
