package com.example.jbt.omdb;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;

public class EditActivity extends AppCompatActivity {

    private static final int REQUEST_TAKE_PHOTO = 1;

    private EditText mSubjectET;
    private EditText mBodyET;
    private TextView mInvisibleTV;
    private EditText mUrlET;
    private Button mShowCaptureBtn;
    private ImageView mPosterImageView;
    private ProgressBar mProgBar;

    private Movie mMovie;

    private String mShowText;
    private String mCaptureText;
    private String mHttpScheme;
    private String mFileScheme;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        mShowText = getResources().getString(R.string.show_capture_button_show);
        mCaptureText = getResources().getString(R.string.show_capture_button_capture);
        mHttpScheme = getResources().getString(R.string.http_scheme);
        mFileScheme = getResources().getString(R.string.file_scheme);

        mSubjectET = ((EditText) findViewById(R.id.subjectEditText));
        mBodyET = ((EditText) findViewById(R.id.bodyEditText));
        mUrlET = ((EditText) findViewById(R.id.urlEditText));
        mInvisibleTV = ((TextView) findViewById(R.id.invisibleTextView));
        mShowCaptureBtn = (Button) findViewById(R.id.urlShowCaptureButton);
        mPosterImageView = (ImageView) findViewById(R.id.posterImageView);
        mProgBar = (ProgressBar) findViewById(R.id.downloadProgressBar);
        Button okBtn = (Button) findViewById(R.id.okButton);
        Button cancelBtn = (Button) findViewById(R.id.cancelButton);

        Intent intent = getIntent();
        mMovie = intent.getParcelableExtra(WebSearchActivity.INTENT_MOVIE_KEY);

        mSubjectET.setText(mMovie.getSubject());
        mBodyET.setText(mMovie.getBody());
        mUrlET.setText(mMovie.getUrl());
        mProgBar.setVisibility(View.INVISIBLE);
        Bitmap image = mMovie.getImage();

        if (image != null)
            mPosterImageView.setImageBitmap(image);

        setShowCaptureButtonText();

        mUrlET.addTextChangedListener(new TextWatcher() {

            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                setShowCaptureButtonText();
            }
        });

        if (cancelBtn != null)
            cancelBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });

        if (okBtn != null)
            okBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (saveMovieToDB())
                        finish();
                }
            });

        mShowCaptureBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if ( mShowCaptureBtn.getText().equals(mCaptureText)) {
                    takePicture();
                    return;
                }

                Uri uri = Uri.parse(mUrlET.getText().toString());

                if (uri.getScheme().equals(mFileScheme)) {
                    displayImageFromGallery(uri.getPath());
                    return;
                }

                if (uri.getScheme().equals(mHttpScheme))
                    new omdbImageDownloadAsyncTask().execute(uri.toString());
            }
        });

        Utility.hideKeyboard(this);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode != REQUEST_TAKE_PHOTO || resultCode != RESULT_OK)
            return;

        mUrlET.setText(mInvisibleTV.getText());

        Uri uri = Uri.parse(mUrlET.getText().toString());
        String path = uri.getPath();

        if ( ! path.isEmpty())
            saveImageToGallery(path);
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

            String errorMsg = getResources().getString(R.string.show_error_msg);
            Toast.makeText(EditActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
        }
    }


    private boolean saveMovieToDB()
    {
        long _id = mMovie.getId();
        String subject = mSubjectET.getText().toString();
        String body = mBodyET.getText().toString();
        String url = mUrlET.getText().toString();
        String imdbid = mMovie.getImdbId();

        BitmapDrawable bitmapDrawable = (BitmapDrawable)mPosterImageView.getDrawable();
        Bitmap image = bitmapDrawable == null ? null : bitmapDrawable.getBitmap();

        if (subject.isEmpty()) {
            String emptyMsg = getResources().getString(R.string.subject_must_not_be_empty);
            Toast.makeText(EditActivity.this, emptyMsg, Toast.LENGTH_SHORT).show();
            return false;
        }

        Movie movie = new Movie(_id, subject, body, url, imdbid, image);
        MoviesDBHelper dbHelper = new MoviesDBHelper(EditActivity.this);

        if ( dbHelper.updateOrInsertMovie(movie) ) {
            String movieSavedMsg = getResources().getString(R.string.movie_saved_msg);
            Toast.makeText(EditActivity.this, movieSavedMsg, Toast.LENGTH_SHORT).show();
        }

        return true;
    }

    private void setShowCaptureButtonText()
    {
        String showCaptureText = mUrlET.getText().toString().isEmpty() ? mCaptureText : mShowText;
        mShowCaptureBtn.setText(showCaptureText);
    }


    private void takePicture()
    {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE );

        if (takePictureIntent.resolveActivity(getPackageManager()) == null)
            return;

        File photoFile = ImageHelper.createImageFile(this);

        if (photoFile == null)
            return;

        Uri uri = Uri.fromFile(photoFile);

        mInvisibleTV.setText(uri.toString());
        takePictureIntent.putExtra(MediaStore. EXTRA_OUTPUT, uri);
        startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
    }


    private void saveImageToGallery(String path)
    {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(path);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }
}
