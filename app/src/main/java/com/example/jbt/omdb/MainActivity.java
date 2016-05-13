package com.example.jbt.omdb;

import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

public class MainActivity extends AppCompatActivity {

    private MovieAdapter mAdapter;
    private MoviesDBHelper mDbHelper;

    private Button mWebBtn;
    private Button mManBtn;
    private ListView mListView;

    public static final String LOG_CAT = "OMDB:";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mWebBtn = (Button)findViewById(R.id.gotoWebButton);
        mManBtn = (Button)findViewById(R.id.gotoManualButton);
        mListView = (ListView)findViewById(R.id.mainListView);

        mDbHelper = new MoviesDBHelper(this);
        mDbHelper.deleteAllSearchResult();

        mAdapter = new MovieAdapter(this, mDbHelper.GetDetailsMovieCursor(), 0);
        mListView.setAdapter(mAdapter);

        mWebBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, WebSearchActivity.class);
                startActivity(intent);
            }
        });

        mManBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Movie movie = new Movie("");
                Intent intent = new Intent(MainActivity.this, EditActivity.class);
                intent.putExtra(WebSearchActivity.INTENT_MOVIE_KEY, movie);
                startActivity(intent);
            }
        });

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Cursor c = (Cursor) parent.getItemAtPosition(position);

                if (c == null)
                    return;

                long _id = c.getInt(c.getColumnIndex(MoviesDBHelper.DETAILS_COL_ID));
                String subject = c.getString(c.getColumnIndex(MoviesDBHelper.DETAILS_COL_SUBJECT));
                String body = c.getString( c.getColumnIndex(MoviesDBHelper.DETAILS_COL_BODY) );
                String url = c.getString( c.getColumnIndex(MoviesDBHelper.DETAILS_COL_URL) );
                String imdbid = c.getString( c.getColumnIndex(MoviesDBHelper.DETAILS_COL_IMDBID) );
                Movie movie = new Movie(_id, subject, body, url, imdbid);

                Intent intent = new Intent(MainActivity.this, EditActivity.class);
                intent.putExtra(WebSearchActivity.INTENT_MOVIE_KEY, movie);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAdapter.changeCursor(mDbHelper.GetDetailsMovieCursor());
    }
}
