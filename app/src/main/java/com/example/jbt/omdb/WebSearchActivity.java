package com.example.jbt.omdb;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.net.URL;
import java.util.ArrayList;

public class WebSearchActivity extends AppCompatActivity {

    public static final String INTENT_MOVIE_KEY = "movie";

    private ArrayAdapter<Movie> mAdapter;
    private OmdbSearchAsyncTask mOmdbSearchAsyncTask;

    private EditText mSearchET;
    private ProgressDialog mProgDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_search);

        mSearchET = (EditText)findViewById(R.id.searchEditText);
        Button goBtn = (Button) findViewById(R.id.goButton);
        Button cancelBtn = (Button) findViewById(R.id.cancelButton);
        ListView list = (ListView)findViewById(R.id.moviesListView);

        if(mSearchET == null || list == null || goBtn == null || cancelBtn == null)
            return;

        mAdapter = new ArrayAdapter<>(this, R.layout.search_list_item);
        list.setAdapter(mAdapter);

        goBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String searchValue = mSearchET.getText().toString();

                if (!searchValue.isEmpty()) {
                    mOmdbSearchAsyncTask = new OmdbSearchAsyncTask();
                    mOmdbSearchAsyncTask.execute(searchValue);
                }
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                String searchTitle = mAdapter.getItem(position).toString();

                if (!searchTitle.isEmpty())
                    new OmdbDetaildAsyncTask().execute(searchTitle);
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();

        MoviesDBHelper dbHelper = new MoviesDBHelper(this);
        ArrayList<Movie> list = dbHelper.getAllSearchResults();
        RefreshSearchList(list);
    }

    private void RefreshSearchList(ArrayList<Movie> list)
    {
        mAdapter.clear();
        mAdapter.addAll(list);
    }


    private class OmdbSearchAsyncTask extends AsyncTask<String, Integer, ArrayList<Movie>>
    {
        private boolean mCancelRequested = false;
        private int mTotalResults;

        private ArrayList<Movie> GetNextPageFromOMDB(URL url)
        {
            NetworkHelper networkHelper = new NetworkHelper(url);
            String jsonString = networkHelper.GetJsonString();

            if (jsonString == null)
                return null;

            OmdbHelper omdbHelper = new OmdbHelper(WebSearchActivity.this);
            int totalRes = omdbHelper.GetTotalResult(jsonString);

            if (mTotalResults == 0 && totalRes> 0 )
                mTotalResults = totalRes;

            return omdbHelper.GetMoviesTitlesOnly(jsonString);
        }

        @Override
        protected void onPreExecute() {

            MoviesDBHelper dbHelper = new MoviesDBHelper(WebSearchActivity.this);
            dbHelper.deleteAllSearchResult();

            Utility.RestrictDeviceOrientation(WebSearchActivity.this);

            String msg =  getResources().getString(R.string.progress_bar_message);
            String enoughString = getResources().getString(R.string.enough_button);
            mProgDialog = new ProgressDialog(WebSearchActivity.this);
            mProgDialog.setMessage(msg);
            mProgDialog.setIndeterminate(false);
            mProgDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mProgDialog.setCancelable(false);

            mProgDialog.setButton(DialogInterface.BUTTON_NEGATIVE, enoughString, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mOmdbSearchAsyncTask.setCancelRequested(true);
                    dialog.dismiss();
                }
            });

            mProgDialog.show();
        }

        @Override
        protected void onPostExecute(ArrayList<Movie> list) {

            if( list != null ) {
                MoviesDBHelper dbHelper = new MoviesDBHelper(WebSearchActivity.this);
                dbHelper.bulkInsertSearchResults(list.toArray(new Movie[list.size()]));
                RefreshSearchList(list);
            }

            Utility.ReleaseDeviceOrientationRestriction(WebSearchActivity.this);
            mSearchET.setText("");
            mProgDialog.dismiss();
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            mProgDialog.setMax(progress[0]);
            mProgDialog.setProgress(progress[1]);
        }

        @Override
        protected ArrayList<Movie> doInBackground(String... params) {

            String searchPhrase = params[0];
            OmdbHelper omdbHelper = new OmdbHelper(WebSearchActivity.this);

            mTotalResults = 0;
            ArrayList<Movie> all = new ArrayList<>();
            ArrayList<Movie> page = new ArrayList<>();

            for(int pageNum = 1; page != null && !mCancelRequested; pageNum++) {

                URL url = omdbHelper.GetSearchURL(searchPhrase, pageNum);
                page = GetNextPageFromOMDB(url);

                if (page != null) {
                    all.addAll(page);
                    publishProgress(mTotalResults, all.size());
                }
            }

            return all;
        }

        public void setCancelRequested(boolean CancelRequested) {
            mCancelRequested = CancelRequested;
        }
    }


    private class OmdbDetaildAsyncTask extends AsyncTask<String, Void, Movie> {

        @Override
        protected Movie doInBackground(String... params) {

            Movie movie = null;
            OmdbHelper omdbHelper = new OmdbHelper(WebSearchActivity.this);

            String movieTitle = params[0];
            URL url = omdbHelper.GetDetailsURL(movieTitle);

            NetworkHelper networkHelper = new NetworkHelper(url);
            String jsonString = networkHelper.GetJsonString();

            if (jsonString != null )
                movie = omdbHelper.GetMovieDetails(jsonString);

            return movie;
        }

        @Override
        protected void onPostExecute(Movie movie) {

            if (movie == null)
                return;

            Intent intent = new Intent(WebSearchActivity.this, EditActivity.class);
            intent.putExtra(WebSearchActivity.INTENT_MOVIE_KEY, movie);
            startActivity(intent);
        }
    }
}
