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
import android.widget.ListView;
import android.widget.SearchView;
import java.net.URL;
import java.util.ArrayList;

public class WebSearchActivity extends AppCompatActivity {

    private ArrayAdapter<Movie> mAdapter;
    private OmdbSearchAsyncTask mOmdbSearchAsyncTask;

    private SearchView mSearchSV;
    private ProgressDialog mProgDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Utility.setContentViewWithLocaleChange(this, R.layout.activity_web_search, R.string.web_search_name);

        mSearchSV = (SearchView) findViewById(R.id.searchSearchView);

        Button cancelBtn = (Button) findViewById(R.id.cancelButton);
        ListView list = (ListView)findViewById(R.id.moviesListView);

        if(list == null || cancelBtn == null)
            return;

        mAdapter = new ArrayAdapter<>(this, R.layout.search_list_item);
        list.setAdapter(mAdapter);

        mSearchSV.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {

                if (query.isEmpty())
                    return false;

                mOmdbSearchAsyncTask = new OmdbSearchAsyncTask();
                mOmdbSearchAsyncTask.execute(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
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
                    new OmdbDetailedAsyncTask().execute(searchTitle);
            }
        });
    }


    @Override
    protected void onResume() { // support list availability on various cases, including device orientation
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
        private boolean mCancelRequested = false; // allow user to request cancelation during download
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

            // Restricts device orientation during download not to lose results on device rotation
            // However, I do provide "show Results" button as escape alternative - see listener below
            Utility.RestrictDeviceOrientation(WebSearchActivity.this);

            String msg =  getString(R.string.progress_bar_message);
            String enoughString = getString(R.string.enough_button);
            mProgDialog = new ProgressDialog(WebSearchActivity.this);
            mProgDialog.setMessage(msg);
            mProgDialog.setIndeterminate(false);
            mProgDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mProgDialog.setCancelable(false);

            mProgDialog.setButton(DialogInterface.BUTTON_NEGATIVE, enoughString, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mOmdbSearchAsyncTask.setCancelRequested();
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
            mSearchSV.setQuery("",false); // for user convenience
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

                if (page != null) { // page == null is either download error or last page available
                    all.addAll(page);
                    publishProgress(mTotalResults, all.size());
                }
            }

            return all;
        }

        public void setCancelRequested() { // responds to progress bar "Show Results" button
            mCancelRequested = true;
        }
    }


    private class OmdbDetailedAsyncTask extends AsyncTask<String, Void, Movie> {

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

            MoviesDBHelper dbHelper = new MoviesDBHelper(WebSearchActivity.this);
            dbHelper.updateOrInsertEditMovie(movie);

            Intent intent = new Intent(WebSearchActivity.this, EditActivity.class);
            startActivity(intent);
        }
    }
}
