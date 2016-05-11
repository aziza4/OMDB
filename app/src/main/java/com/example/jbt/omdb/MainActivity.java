package com.example.jbt.omdb;

import android.app.Dialog;
import android.app.ProgressDialog;
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

            String searchValue = params[0];

            // example: http://www.omdbapi.com/?s=sunday&r=json&page=1
            // --------------------------------------------------------------------
            final String scheme = getResources().getString(R.string.scheme);
            final String authority = getResources().getString(R.string.authority);
            final String searchKey = getResources().getString(R.string.omdb_search_key);
            final String dataTypeKey = getResources().getString(R.string.omdb_data_type_key);
            final String dataTypeValue = getResources().getString(R.string.omdb_data_type_value);
            final String pageKey = getResources().getString(R.string.omdb_page_key);

            totalResults = 0;
            ArrayList<Movie> all = new ArrayList<>();
            ArrayList<Movie> page = new ArrayList<>(); // start with non-null value

            try {

                for(int i=1; page != null && !cancelRequested ; i++) {

                    Uri.Builder builder = new Uri.Builder();
                    builder.scheme(scheme)
                            .authority(authority)
                            .appendQueryParameter(searchKey, searchValue)
                            .appendQueryParameter(dataTypeKey, dataTypeValue)
                            .appendQueryParameter(pageKey, ""+i);

                    URL url = new URL(builder.build().toString());
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
                final String FIELD_NAME = getResources().getString(R.string.omdb_res_title_field);
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

                    if (user.has(FIELD_NAME))
                        list.add( new Movie(user.getString(FIELD_NAME)));
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

            String searchTitleValue = params[0];

            // example: http://www.omdbapi.com/?t=Matrix&y=&plot=full&r=json
            // --------------------------------------------------------------------
            final String scheme = getResources().getString(R.string.scheme);
            final String authority = getResources().getString(R.string.authority);
            final String searcTitleKey = getResources().getString(R.string.omdb_search_title_key);
            final String plotKey = getResources().getString(R.string.omdb_plot_key);
            final String plotValue = getResources().getString(R.string.omdb_plot_value);
            final String dataTypeKey = getResources().getString(R.string.omdb_data_type_key);
            final String dataTypeValue = getResources().getString(R.string.omdb_data_type_value);

            Uri.Builder builder = new Uri.Builder();
            builder.scheme(scheme)
                    .authority(authority)
                    .appendQueryParameter(searcTitleKey, searchTitleValue)
                    .appendQueryParameter(plotKey, plotValue)
                    .appendQueryParameter(dataTypeKey, dataTypeValue);

            try {

                URL url = new URL(builder.build().toString());

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
