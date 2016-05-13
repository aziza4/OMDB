package com.example.jbt.omdb;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;

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
                if (c != null) {
                    Movie movie = Utility.getMovieFromCursor(c);
                    luanchEditActivity(movie);
                }
            }
        });

        registerForContextMenu(mListView);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {

        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_context_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        int position = ((AdapterView.AdapterContextMenuInfo)item.getMenuInfo()).position;
        Cursor c = mAdapter.getCursor();
        if (c == null) return false;

        c.moveToPosition(position);
        Movie movie = Utility.getMovieFromCursor(c);

        switch (item.getItemId())
        {
            case R.id.editMenuItem:
                luanchEditActivity(movie);
                return true;

            case R.id.deleteMenuItem:
                mDbHelper.deleteMovie(movie.getId());
                RefreshMainList();
                return true;

            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        RefreshMainList();
    }


    public void luanchEditActivity(Movie movie)
    {
        Intent intent = new Intent(MainActivity.this, EditActivity.class);
        intent.putExtra(WebSearchActivity.INTENT_MOVIE_KEY, movie);
        startActivity(intent);
    }

    private void RefreshMainList() {
        mAdapter.changeCursor(mDbHelper.GetDetailsMovieCursor());
    }
}
