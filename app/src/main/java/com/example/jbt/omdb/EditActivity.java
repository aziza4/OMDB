package com.example.jbt.omdb;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ShareActionProvider;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.util.Locale;

public class EditActivity extends AppCompatActivity {

    private static final int REQUEST_TAKE_PHOTO = 1;
    private static final float SEEK_BAR_FACTOR = 10f;

    private EditText mSubjectET;
    private EditText mBodyET;
    private TextView mInvisibleTV;
    private EditText mUrlET;
    private Button mShowCaptureBtn;
    private ImageView mPosterImageView;
    private ProgressBar mProgBar;
    private TextView mSeekBarTV;

    private Movie mMovie;

    private String mShowText;
    private String mCaptureText;
    private String mHttpScheme;
    private String mFileScheme;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Utility.setContentViewWithLocaleChange(this, R.layout.activity_edit, R.string.edit_name);

        mShowText = getString(R.string.show_capture_button_show);
        mCaptureText = getString(R.string.show_capture_button_capture);
        mHttpScheme = getString(R.string.http_scheme);
        mFileScheme = getString(R.string.file_scheme);

        mSubjectET = ((EditText) findViewById(R.id.subjectEditText));
        mBodyET = ((EditText) findViewById(R.id.bodyEditText));
        mUrlET = ((EditText) findViewById(R.id.urlEditText));
        mInvisibleTV = ((TextView) findViewById(R.id.invisibleTextView));
        mShowCaptureBtn = (Button) findViewById(R.id.urlShowCaptureButton);
        mPosterImageView = (ImageView) findViewById(R.id.posterImageView);
        mProgBar = (ProgressBar) findViewById(R.id.downloadProgressBar);
        mSeekBarTV = (TextView) findViewById(R.id.ratingValueTextView);
        Button okBtn = (Button) findViewById(R.id.okButton);
        Button cancelBtn = (Button) findViewById(R.id.cancelButton);
        SeekBar seekBar = (SeekBar) findViewById(R.id.ratingSeekBar);

        Intent intent = getIntent();
        mMovie = intent.getParcelableExtra(WebSearchActivity.INTENT_MOVIE_KEY);

        String ratingStr = "" + mMovie.getRating();
        int ratingValue = (int) (mMovie.getRating() * SEEK_BAR_FACTOR);
        mSeekBarTV.setText(ratingStr);
        mSubjectET.setText(mMovie.getSubject());
        mBodyET.setText(mMovie.getBody());
        mUrlET.setText(mMovie.getUrl());
        mProgBar.setVisibility(View.INVISIBLE);

        if (seekBar != null)
            seekBar.setProgress(ratingValue);

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

        if (seekBar != null)
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    String value = String.format(Locale.ENGLISH, "%.1f", progress/ SEEK_BAR_FACTOR);
                    mSeekBarTV.setText(value);
                }

                @Override public void onStartTrackingTouch(SeekBar seekBar) {}
                @Override public void onStopTrackingTouch(SeekBar seekBar) {}
            });

        Utility.hideKeyboard(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.edit_menu, menu);
        MenuItem item = menu.findItem(R.id.action_share);
        ShareActionProvider mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);

        if (mShareActionProvider != null) {

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
            }
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, mMovie.getDetailsAsText(this));

            mShareActionProvider.setShareIntent(shareIntent);
        }

        return true;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode != REQUEST_TAKE_PHOTO || resultCode != RESULT_OK)
            return;

        mUrlET.setText(mInvisibleTV.getText()); // save data in view to accommodate device rotation

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

            String errorMsg = getString(R.string.show_error_msg);
            Toast.makeText(EditActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
        }
    }


    private boolean saveMovieToDB()
    {
        long _id = mMovie.getId();
        String subject = mSubjectET.getText().toString();
        String body = mBodyET.getText().toString();
        String url = mUrlET.getText().toString();
        float rating = Float.parseFloat(mSeekBarTV.getText().toString());
        String imdbid = mMovie.getImdbId();

        BitmapDrawable bitmapDrawable = (BitmapDrawable)mPosterImageView.getDrawable();
        Bitmap image = bitmapDrawable == null ? null : bitmapDrawable.getBitmap();

        if (subject.isEmpty()) {
            String emptyMsg = getString(R.string.subject_must_not_be_empty);
            Toast.makeText(EditActivity.this, emptyMsg, Toast.LENGTH_SHORT).show();
            return false;
        }

        Movie movie = new Movie(_id, subject, body, url, imdbid, rating, image);
        MoviesDBHelper dbHelper = new MoviesDBHelper(EditActivity.this);

        if ( dbHelper.updateOrInsertMovie(movie) ) {
            String movieSavedMsg = getString(R.string.movie_saved_msg);
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
