package com.example.jbt.omdb;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

public class EditActivity extends AppCompatActivity {

    private EditText mSubjectET;
    private EditText mBodyET;
    private EditText mUrlET;
    private Button mShowBtn;
    private Button mOkBtn;
    private Button mCancelBtn;
    private ImageView mPosterImageView;
    private ProgressBar mProgBar;

    private Movie mMovie;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        mSubjectET = ((EditText) findViewById(R.id.subjectEditText));
        mBodyET = ((EditText) findViewById(R.id.bodyEditText));
        mUrlET = ((EditText) findViewById(R.id.urlEditText));
        mShowBtn = (Button) findViewById(R.id.urlShowButton);
        mOkBtn = (Button) findViewById(R.id.okButton);
        mCancelBtn = (Button) findViewById(R.id.cancelButton);
        mPosterImageView = (ImageView) findViewById(R.id.posterImageView);
        mProgBar = (ProgressBar) findViewById(R.id.downloadProgressBar);

        Intent intent = getIntent();
        mMovie = intent.getParcelableExtra(WebSearchActivity.INTENT_MOVIE_KEY);

        mSubjectET.setText(mMovie.getSubject());
        mBodyET.setText(mMovie.getBody());
        mUrlET.setText(mMovie.getUrl());
        mProgBar.setVisibility(View.INVISIBLE);

        if ( mMovie.isSavedInDB() && mMovie.getImage() != null) {
            // The following 2 lines are bypass to android BUG (cannot parcel bitmap...)
            MoviesDBHelper dbHelper = new MoviesDBHelper(EditActivity.this);
            mPosterImageView.setImageBitmap(dbHelper.GetMovie(mMovie.getId()).getImage());
        }

        mShowBtn.setEnabled(Utility.isValidUrl(mUrlET.getText().toString()));

        mUrlET.addTextChangedListener(new TextWatcher() {

            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override public void afterTextChanged(Editable s) {
                mShowBtn.setEnabled(Utility.isValidUrl(s.toString()));
            }
        });

        mCancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mOkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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
                    return;
                }

                Movie movie = new Movie(_id, subject, body, url, imdbid, image);
                MoviesDBHelper dbHelper = new MoviesDBHelper(EditActivity.this);

                if ( dbHelper.updateOrInsertMoview(movie) ) {
                    String movieSavedMsg = getResources().getString(R.string.movie_saved_msg);
                    Toast.makeText(EditActivity.this, movieSavedMsg, Toast.LENGTH_SHORT).show();
                }

                finish();
            }
        });

        mShowBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                String url = mUrlET.getText().toString();
                new omdbImageDownloadAsyncTask().execute(url);
            }
        });
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

            if (image != null)
                mPosterImageView.setImageBitmap(image);
        }
    }

}
