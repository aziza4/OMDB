package com.example.jbt.omdb;



import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;


public class FullPosterFragment extends Fragment {


    private ImageView mPosterImageView;

    private MoviesDBHelper mDbHelper;
    private OnPosterFragListener mListener;


    public FullPosterFragment() { }


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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View viewRoot = inflater.inflate(R.layout.fragment_full_poster, container, false);

        mDbHelper = new MoviesDBHelper(getActivity());

        mPosterImageView = (ImageView) viewRoot.findViewById(R.id.fullPosterImageView);
        ImageView mCloseButton = (ImageView) viewRoot.findViewById(R.id.closeFullPosterImageView);

        mCloseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onClose();
            }
        });

        return viewRoot;
    }

    @Override
    public void onResume() {
        super.onResume();

        Movie movie = mDbHelper.getEditMovie();
        mPosterImageView.setImageBitmap(movie.getImage());
    }

    public interface OnPosterFragListener {
        void onClose();
    }
}
