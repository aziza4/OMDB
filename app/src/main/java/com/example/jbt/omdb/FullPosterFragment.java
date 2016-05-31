package com.example.jbt.omdb;



import android.content.Context;
import android.os.Bundle;
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

        View viewRoot = inflater.inflate(R.layout.fragment_full_poster, container, false);

        if (mMovie == null && savedInstanceState != null) {
            mMovie = savedInstanceState.getParcelable(WebSearchActivity.INTENT_MOVIE_KEY);
        }

        ImageView mPosterImageView = (ImageView) viewRoot.findViewById(R.id.fullPosterImageView);
        mPosterImageView.setImageBitmap(mMovie.getImage());

        ImageView mCloseButton = (ImageView) viewRoot.findViewById(R.id.closeFullPosterImageView);

        mCloseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onPosterClose(mMovie);
            }
        });

        return viewRoot;
    }

    public interface OnPosterFragListener {
        void onPosterClose(Movie movie);
    }
}
