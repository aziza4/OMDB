package com.example.jbt.omdb;


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

    public static final int REQUEST_TAKE_PHOTO = 1;
    private static final float SEEK_BAR_FACTOR = 10f;

    private Movie mMovie;
    private MoviesDBHelper mDbHelper;

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
    private OnEditDoneListener mListener;

    public EditFragment() {}

    public void setTabletMode(boolean isTabletMode) { mIsTabletMode = isTabletMode; }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mListener = (OnEditDoneListener) context; // update parent activity that movie was saved
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mDbHelper = new MoviesDBHelper(getActivity());

        View viewRoot = inflater.inflate(R.layout.fragment_edit, container, false);

        mHasCamera = getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);

        mShowText = getString(R.string.show_capture_button_show);
        mCaptureText = getString(R.string.show_capture_button_capture);
        mHttpScheme = getString(R.string.http_scheme);
        mFileScheme = getString(R.string.file_scheme);

        mSubjectET = ((EditText) viewRoot.findViewById(R.id.subjectEditText));
        mBodyET = ((EditText) viewRoot.findViewById(R.id.bodyEditText));
        mUrlET = ((EditText) viewRoot.findViewById(R.id.urlEditText));
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

        setHasOptionsMenu(true); // enable 'Share' action bar item from fragment

        return viewRoot;
    }


    @Override
    public void onStart() {
        super.onStart();
        refreshLayout(); // via load data from db
    }


    @Override
    public void onStop() {
        super.onStop();
        saveLayout(); // to mitigate device rotation at any time
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


    private void saveLayout()
    {
        Movie movie = getMovieFromLayout();

        FragmentHelper fragmentHelper = new FragmentHelper(getActivity(), mIsTabletMode);
        fragmentHelper.replaceMovieOnEditFragment(movie);
    }


    private void setShowCaptureButtonText()
    {
        // "Show" toggle to "Capture" if: device has camera, and url string is blank
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

        mUrlET.setText(uri.toString()); // save file url to url EditText view
        saveLayout(); // save movie immediately to db to mitigate device rotation

        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        getActivity().startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
    }


    public void onCameraActivityResult()
    {
        String url = mUrlET.getText().toString();
        Uri uri = Uri.parse(url);
        String path = uri.getPath();

        if ( ! path.isEmpty())
            saveImageToGallery(path);
    }


    private void saveImageToGallery(String path)
    {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(path);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        getActivity().sendBroadcast(mediaScanIntent);
    }


    private void SetViewsValues(Movie movie)
    {
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

        if (mIsTabletMode)
            mCancelBtn.setVisibility(View.GONE);

        setShowCaptureButtonText();

        Utility.hideKeyboard(getActivity()); // stop irritating auto keyboard popup
    }


    public void refreshLayout()
    {
        mMovie = mDbHelper.getEditMovie();
        SetViewsValues(mMovie);
    }


    public interface OnEditDoneListener {
        void onMovieSaved();
    }
}
