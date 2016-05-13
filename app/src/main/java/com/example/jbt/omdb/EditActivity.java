package com.example.jbt.omdb;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class EditActivity extends AppCompatActivity {

    private EditText mSubjectET;
    private EditText mBodyET;
    private EditText mUrlET;
    private Button mOkBtn;
    private Button mCancelBtn;

    private Movie mMovie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        mSubjectET = ((EditText) findViewById(R.id.subjectEditText));
        mBodyET = ((EditText) findViewById(R.id.bodyEditText));
        mUrlET = ((EditText) findViewById(R.id.urlEditText));
        mOkBtn = (Button) findViewById(R.id.okButton);
        mCancelBtn = (Button) findViewById(R.id.cancelButton);

        Intent intent = getIntent();
        mMovie = (Movie)intent.getSerializableExtra(WebSearchActivity.INTENT_MOVIE_KEY);

        mSubjectET.setText(mMovie.getSubject());
        mBodyET.setText(mMovie.getBody());
        mUrlET.setText(mMovie.getUrl());

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

                Movie movie = new Movie(_id, subject, body, url, imdbid);
                MoviesDBHelper dbHelper = new MoviesDBHelper(EditActivity.this);

                if ( dbHelper.updateOrInsertMoview(movie) ) {
                    String movieSavedMsg = getResources().getString(R.string.movie_saved_msg);
                    Toast.makeText(EditActivity.this, movieSavedMsg, Toast.LENGTH_SHORT).show();
                }

                finish();
            }
        });
    }
}
