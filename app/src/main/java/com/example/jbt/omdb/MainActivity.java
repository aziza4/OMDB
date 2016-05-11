package com.example.jbt.omdb;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
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

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    final String LOG_CAT = "OMDB:";

    private ArrayAdapter<Movie> adapter;
    private OmdbSearchAsyncTask omdbSearchAsyncTask;

    private EditText searchET;
    private ProgressDialog pDialog;
    private static final int progress_bar_type = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
            case progress_bar_type:
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

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showDialog(progress_bar_type);
        }

        @Override
        protected void onPostExecute(ArrayList<Movie> list) {

            dismissDialog(progress_bar_type);
            searchET.setText("");

            if(list == null)
                return;

            adapter.clear();
            adapter.addAll(list);

        }

        protected void onProgressUpdate(Integer... progress) {
            pDialog.setMax(progress[1]);
            pDialog.setProgress(progress[0]);
        }

        @Override
        protected ArrayList<Movie> doInBackground(String... params) {

            String searchPhrase = params[0];

            totalResults = 0;
            ArrayList<Movie> all = new ArrayList<>();
            ArrayList<Movie> page = new ArrayList<>(); // start with non-null value

            try {

                for(int pageNum=1; page != null && !cancelRequested ; pageNum++) {

                    String urlString = Utility.getSearchPhraseUrlString(MainActivity.this, searchPhrase, pageNum);
                    URL url = new URL(urlString);
                    page = GetNextPageFromOMDB(url);

                    if (page != null) {
                        all.addAll(page);
                        publishProgress(all.size(), totalResults);
                    }
                }

            } catch (MalformedURLException e) {
                Log.e(LOG_CAT, e.getMessage());
            }

            return all;
        }

        private ArrayList<Movie> GetNextPageFromOMDB(URL url)
        {
            ArrayList<Movie> list = new ArrayList<>();
            HttpURLConnection con = null;

            try {

                con = (HttpURLConnection)url.openConnection();

                int resCode = con.getResponseCode();

                if (resCode != HttpURLConnection.HTTP_OK)
                    return null;

                BufferedReader r = new BufferedReader(new InputStreamReader(con.getInputStream()));

                String result = "", line;
                while((line = r.readLine()) != null) {
                    result += line;
                }

                final String MAIN_OBJECT_NAME = getResources().getString(R.string.omdb_res_main_obj);
                final String TITLE_NAME = getResources().getString(R.string.omdb_res_title_field);
                final String RESPONSE_NAME = getResources().getString(R.string.omdb_res_response_field);
                final String TOTAL_RESULTS_NAME = getResources().getString(R.string.omdb_res_total_results_field);

                JSONObject searchObj = new JSONObject(result);

                if (totalResults == 0 && searchObj.has(TOTAL_RESULTS_NAME))
                    totalResults = searchObj.getInt(TOTAL_RESULTS_NAME);

                if (searchObj.has(RESPONSE_NAME) && !searchObj.getBoolean(RESPONSE_NAME))
                        return null;

                JSONArray array = searchObj.getJSONArray(MAIN_OBJECT_NAME);
                for (int i=0; i< array.length(); i++)
                {
                    JSONObject user = array.getJSONObject(i);

                    if (user.has(TITLE_NAME))
                        list.add( new Movie(user.getString(TITLE_NAME)));
                }

            } catch (Exception e) {

                Log.e(LOG_CAT, e.getMessage());

            } finally {

                if (con != null)
                    con.disconnect();
            }

            return list;
        }


        public void setCancelRequested(boolean cancelRequested) {
            this.cancelRequested = cancelRequested;
        }
    }


    public class OmdbDetaildAsyncTask extends AsyncTask<String, Void, Movie> {

        @Override
        protected Movie doInBackground(String... params) {

            HttpURLConnection con = null;

            try {

                String searchTitle = params[0];
                String urlString = Utility.getDetailsUrlString(MainActivity.this, searchTitle);
                URL url = new URL(urlString);

                con = (HttpURLConnection)url.openConnection();

                int resCode = con.getResponseCode();

                if (resCode != HttpURLConnection.HTTP_OK)
                    return null;

                BufferedReader r = new BufferedReader(new InputStreamReader(con.getInputStream()));

                String result = "", line;
                while((line = r.readLine()) != null) {
                    result += line;
                }

                final String TITLE_NAME = getResources().getString(R.string.omdb_res_title_field);
                final String PLOT_NAME = getResources().getString(R.string.omdb_res_plot_field);
                final String POSTER_NAME = getResources().getString(R.string.omdb_res_poster_field);
                final String IMDBID_NAME = getResources().getString(R.string.omdb_res_imdbid_field);

                JSONObject searchObj = new JSONObject(result);

                String subject = searchObj.has(TITLE_NAME) ? searchObj.getString(TITLE_NAME) : "";
                String body = searchObj.has(PLOT_NAME) ? searchObj.getString(PLOT_NAME) : "";
                String posterUrl = searchObj.has(POSTER_NAME) ? searchObj.getString(POSTER_NAME) : "";
                String imdbid = searchObj.has(IMDBID_NAME) ? searchObj.getString(IMDBID_NAME) : "";


                return new Movie(subject, body, posterUrl, imdbid);

            } catch (Exception e) {

                Log.e(LOG_CAT, e.getMessage());

            } finally {

                if (con != null)
                    con.disconnect();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Movie movie) {

            if (movie == null)
                return;

            String msg = String.format(" Title=%s\n Plot=%s\n Poster=%s\n IMDBID=%s",
                    movie.getSubject(), movie.getBody().substring(0,10), movie.getUrl().substring(0,10), movie.getImdbId());

            Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
        }

    }
}
