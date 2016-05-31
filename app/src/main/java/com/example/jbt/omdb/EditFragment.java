package com.example.jbt.omdb;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.Locale;


public class EditFragment extends Fragment {

    private static final int REQUEST_TAKE_PHOTO = 1;
    private static final float SEEK_BAR_FACTOR = 10f;

    private Movie mMovie;
    private MoviesDBHelper mDbHelper;

    private View mRootLayout;
    private EditText mSubjectET;
    private EditText mBodyET;
    private EditText mUrlET;
    private Button mShowCaptureBtn;
    private ImageView mPosterImageView;
    private ProgressBar mProgBar;
    private SeekBar mSeekBar;
    private TextView mSeekBarTV;
    private Button mCancelBtn;

    private String mShowText;
    private String mCaptureText;
    private String mHttpScheme;
    private String mFileScheme;

    private boolean mHasCamera;
    private boolean mIsTabletMode;
    private OnEditFragListener mListener;

    public EditFragment() {}

    public void setMovie(Movie movie) { mMovie = movie; }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mListener = (OnEditFragListener) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        mMovie = getMovieFromLayout();
        outState.putParcelable(WebSearchActivity.INTENT_MOVIE_KEY, mMovie);

        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mDbHelper = new MoviesDBHelper(getActivity());

        if (mMovie == null && savedInstanceState != null) {
            mMovie = savedInstanceState.getParcelable(WebSearchActivity.INTENT_MOVIE_KEY);
        }

        mHasCamera = getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);

        SharedPrefHelper sharedPrefHelper = new SharedPrefHelper(getActivity());
        mIsTabletMode = sharedPrefHelper.getTabletMode();

        View viewRoot = inflater.inflate(R.layout.fragment_edit, container, false);

        mShowText = getString(R.string.show_capture_button_show);
        mCaptureText = getString(R.string.show_capture_button_capture);
        mHttpScheme = getString(R.string.http_scheme);
        mFileScheme = getString(R.string.file_scheme);

        mRootLayout = viewRoot.findViewById(R.id.editFragLayout);
        mSubjectET = (EditText) viewRoot.findViewById(R.id.subjectEditText);
        mBodyET = (EditText) viewRoot.findViewById(R.id.bodyEditText);
        mUrlET = (EditText) viewRoot.findViewById(R.id.urlEditText);
        mShowCaptureBtn = (Button) viewRoot.findViewById(R.id.urlShowCaptureButton);
        mPosterImageView = (ImageView) viewRoot.findViewById(R.id.posterImageView);
        mProgBar = (ProgressBar) viewRoot.findViewById(R.id.downloadProgressBar);
        mSeekBar = (SeekBar) viewRoot.findViewById(R.id.ratingSeekBar);
        mSeekBarTV = (TextView) viewRoot.findViewById(R.id.ratingValueTextView);
        Button mOkBtn = (Button) viewRoot.findViewById(R.id.okButton);
        mCancelBtn = (Button) viewRoot.findViewById(R.id.cancelButton);

        mUrlET.addTextChangedListener(new TextWatcher() {

            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                setShowCaptureButtonText();
            }
        });

        mCancelBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getActivity().finish();
                }
            });

        mOkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if ( ! saveMovieToDB()) {
                    String emptyMsg = getString(R.string.subject_must_not_be_empty);
                    Toast.makeText(getActivity(), emptyMsg, Toast.LENGTH_SHORT).show();
                    return;
                }

                if (mIsTabletMode) {
                    mListener.onMovieSaved();
                    return;
                }

                getActivity().finish();

            }
        });

        mShowCaptureBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                Utility.hideKeyboard(getActivity()); // stop irritating auto keyboard popup

                if ( mShowCaptureBtn.getText().toString().equals(mCaptureText)) {
                    takePicture();
                    return;
                }

                Uri uri = Uri.parse(mUrlET.getText().toString());

                if (uri.getScheme() == null)
                    return;

                if (uri.getScheme().equals(mFileScheme)) {
                    displayImageFromGallery(uri.getPath()); // display from gallery
                    return;
                }

                if (uri.getScheme().equals(mHttpScheme))
                    new omdbImageDownloadAsyncTask().execute(uri.toString()); // or get from web
            }
        });


        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                String value = String.format(Locale.ENGLISH, "%.1f", progress/SEEK_BAR_FACTOR); // range 0.0 - 5.0
                mSeekBarTV.setText(value);
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        mPosterImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if ( ((ImageView)v).getDrawable() != null )
                    mListener.onPosterClicked(mMovie);
            }
        });

        setHasOptionsMenu(true); // enable 'Share' action bar item from fragment

        return viewRoot;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode != EditFragment.REQUEST_TAKE_PHOTO)
            return;

        switch (resultCode) {

            case Activity.RESULT_CANCELED:
                mMovie.setUrl(""); // must revert the action
                updateLayout(mMovie);
                break;

            case Activity.RESULT_OK:
                Uri uri = Uri.parse(mMovie.getUrl());
                String path = uri.getPath();

                if (!path.isEmpty())
                    saveImageToGallery(path);
                break;
        }
    }


    @Override
    public void onStart() {
        super.onStart();
        updateLayout(mMovie);
    }


    @Override
    public void onStop() {
        super.onStop();
        mMovie = getMovieFromLayout();
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        inflater.inflate(R.menu.edit_menu, menu);
        MenuItem item = menu.findItem(R.id.action_share);

        ShareActionProvider mShareActionProvider =
                (ShareActionProvider) MenuItemCompat.getActionProvider(item);

        if (mShareActionProvider == null)
            return;

        Intent shareIntent = new Intent(Intent.ACTION_SEND);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);

        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, mMovie.getDetailsAsText(getActivity()));
        mShareActionProvider.setShareIntent(shareIntent); // activate sharing
    }


    private void displayImageFromGallery(String path)
    {
        Bitmap image = ImageHelper.getImageFromGallery(path);

        if (image != null)
            mPosterImageView.setImageBitmap(image);
    }


    private class omdbImageDownloadAsyncTask extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... params) {

            NetworkHelper networkHelper = new NetworkHelper(params[0]);
            return networkHelper.GetImage();
        }

        @Override
        protected void onPreExecute() {

            mProgBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(Bitmap image) {

            mProgBar.setVisibility(View.INVISIBLE);

            if (image != null) {
                mPosterImageView.setImageBitmap(image);
                return;
            }

            String errorMsg = getString(R.string.show_error_msg);
            Toast.makeText(getActivity(), errorMsg, Toast.LENGTH_SHORT).show();
        }
    }


    private Movie getMovieFromLayout()
    {
        long _id = mMovie.getId();
        String subject = mSubjectET.getText().toString();
        String body = mBodyET.getText().toString();
        String url = mUrlET.getText().toString();
        String seekBarText = mSeekBarTV.getText().toString();
        float rating = seekBarText.isEmpty() ? 0f : Float.parseFloat(mSeekBarTV.getText().toString());
        String imdbid = mMovie.getImdbId();

        BitmapDrawable bitmapDrawable = (BitmapDrawable) mPosterImageView.getDrawable();
        Bitmap image = bitmapDrawable == null ? null : bitmapDrawable.getBitmap();

        return new Movie(_id, subject, body, url, imdbid, rating, image);
    }


    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean saveMovieToDB()
    {
        Movie movieToSave = getMovieFromLayout();

        if (movieToSave.getSubject().isEmpty())
            return false;

        if ( mDbHelper.updateOrInsertMovie(movieToSave)) {
            String movieSavedMsg = getString(R.string.movie_saved_msg);
            Toast.makeText(getActivity(), movieSavedMsg, Toast.LENGTH_SHORT).show();
        }

        return true;
    }


    private void setShowCaptureButtonText()
    {
        // "Show" toggles to "Capture" if: device has camera, and url string is blank
        boolean urlEmpty = mUrlET.getText().toString().isEmpty();
        String showCaptureText = mHasCamera && urlEmpty ? mCaptureText : mShowText;
        mShowCaptureBtn.setText(showCaptureText);
    }


    private void takePicture()
    {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE );

        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) == null)
            return;

        File photoFile = ImageHelper.createImageFile(getActivity());

        if (photoFile == null)
            return;

        Uri uri = Uri.fromFile(photoFile);

        mUrlET.setText(uri.toString()); // mMovie will be update on OnStop()

        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
    }

    private void saveImageToGallery(String path)
    {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(path);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        getActivity().sendBroadcast(mediaScanIntent);
    }


    @SuppressWarnings("deprecation")
    private void updateLayout(Movie movie)
    {
        if (mIsTabletMode) {
            mRootLayout.setBackgroundColor(getResources().getColor(R.color.edit_background));
            mCancelBtn.setVisibility(View.GONE);
        }

        String ratingStr = "" + movie.getRating();
        int ratingValue = (int) (movie.getRating() * SEEK_BAR_FACTOR);
        mSeekBarTV.setText(ratingStr);
        mSubjectET.setText(movie.getSubject());
        mBodyET.setText(movie.getBody());
        mUrlET.setText(movie.getUrl());
        mProgBar.setVisibility(View.INVISIBLE);
        mSeekBar.setProgress(ratingValue);

        if (movie.getImage() != null)
            mPosterImageView.setImageBitmap(movie.getImage());
        else
            mPosterImageView.setImageResource(0);

        setShowCaptureButtonText();

        Utility.hideKeyboard(getActivity()); // stop irritating auto keyboard popup
    }


    public interface OnEditFragListener {
        void onMovieSaved();
        void onPosterClicked(Movie movie);
    }
}
