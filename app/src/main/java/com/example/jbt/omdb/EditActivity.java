package com.example.jbt.omdb;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class EditActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        EditText subjectET = ((EditText) findViewById(R.id.subjectEditText));
        EditText bodyET = ((EditText) findViewById(R.id.bodyEditText));
        EditText urlET = ((EditText) findViewById(R.id.urlEditText));

        Intent intent = getIntent();
        Movie movie = (Movie)intent.getSerializableExtra(WebSearchActivity.INTENT_MOVIE_KEY);

        subjectET.setText(movie.getSubject());
        bodyET.setText(movie.getBody());
        urlET.setText(movie.getUrl());

        findViewById(R.id.cancelButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
