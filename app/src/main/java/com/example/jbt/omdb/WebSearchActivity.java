package com.example.jbt.omdb;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class WebSearchActivity extends AppCompatActivity {

    public static final String INTENT_MOVIE_KEY = "movie";
    private static final int PROGRESS_BAR_TYPE = 0;

    private ArrayAdapter<Movie> adapter;
    private OmdbSearchAsyncTask omdbSearchAsyncTask;

    private EditText searchET;
    private ProgressDialog pDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_search);

        searchET = (EditText)findViewById(R.id.searchEditText);
        Button goBtn = (Button) findViewById(R.id.goButton);
        Button cancelBtn = (Button) findViewById(R.id.cancelButton);
        ListView list = (ListView)findViewById(R.id.moviesListView);

        if(searchET == null || list == null || goBtn == null || cancelBtn == null)
            return;

        adapter = new ArrayAdapter<>(this, R.layout.movies_list_item);
        list.setAdapter(adapter);

        goBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String searchValue = searchET.getText().toString();

                if (!searchValue.isEmpty()) {
                    omdbSearchAsyncTask = new OmdbSearchAsyncTask();
                    omdbSearchAsyncTask.execute(searchValue);
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

                String searchTitle = adapter.getItem(position).toString();

                if (!searchTitle.isEmpty())
                    new OmdbDetaildAsyncTask().execute(searchTitle);
            }
        });

    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case PROGRESS_BAR_TYPE:
                String msg =  getResources().getString(R.string.progress_bar_message);
                String cancel = getResources().getString(R.string.cancel_button);
                pDialog = new ProgressDialog(this);
                pDialog.setMessage(msg);
                pDialog.setIndeterminate(false);
                pDialog.setMax(100);
                pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                pDialog.setCancelable(true);
                pDialog.setButton(DialogInterface.BUTTON_NEGATIVE, cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        omdbSearchAsyncTask.setCancelRequested(true);
                        dialog.dismiss();
                    }
                });
                pDialog.show();
                return pDialog;
            default:
                return null;
        }
    }

    public class OmdbSearchAsyncTask extends AsyncTask<String, Integer, ArrayList<Movie>>
    {
        private boolean cancelRequested = false;
        private int totalResults;

        private ArrayList<Movie> GetNextPageFromOMDB(URL url)
        {
            NetworkHelper networkHelper = new NetworkHelper(url);
            String jsonString = networkHelper.GetJsonString();

            if (jsonString == null)
                return null;

            OmdbHelper omdbHelper = new OmdbHelper(WebSearchActivity.this);
            int totalRes = omdbHelper.GetTotalResult(jsonString);

            if (totalResults == 0 && totalRes> 0 )
                totalResults = totalRes;

            return omdbHelper.GetMoviesTitleOnly(jsonString);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showDialog(PROGRESS_BAR_TYPE);
        }

        @Override
        protected void onPostExecute(ArrayList<Movie> list) {

            dismissDialog(PROGRESS_BAR_TYPE);
            searchET.setText("");

            if(list == null)
                return;

            adapter.clear();
            adapter.addAll(list);

        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            pDialog.setMax(progress[1]);
            pDialog.setProgress(progress[0]);
        }

        @Override
        protected ArrayList<Movie> doInBackground(String... params) {

            String searchPhrase = params[0];
            OmdbHelper omdbHelper = new OmdbHelper(WebSearchActivity.this);

            totalResults = 0;
            ArrayList<Movie> all = new ArrayList<>();
            ArrayList<Movie> page = new ArrayList<>();

            for(int pageNum=1; page != null && !cancelRequested ; pageNum++) {

                URL url = omdbHelper.GetSearchURL(searchPhrase, pageNum);
                page = GetNextPageFromOMDB(url);

                if (page != null) {
                    all.addAll(page);
                    publishProgress(all.size(), totalResults);
                }
            }

            return all;
        }

        public void setCancelRequested(boolean cancelRequested) {
            this.cancelRequested = cancelRequested;
        }
    }


    public class OmdbDetaildAsyncTask extends AsyncTask<String, Void, Movie> {

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

            String msg = String.format(" Title=%s\n Plot=%s\n Poster=%s\n IMDBID=%s",
                    movie.getSubject(), movie.getBody().substring(0,10), movie.getUrl().substring(0,10), movie.getImdbId());

            Intent intent = new Intent(WebSearchActivity.this, EditActivity.class);
            intent.putExtra(WebSearchActivity.INTENT_MOVIE_KEY, movie);
            startActivity(intent);

            Toast.makeText(WebSearchActivity.this, msg, Toast.LENGTH_SHORT).show();
        }
    }
}
