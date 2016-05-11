package com.example.jbt.omdb;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class OmdbHelper {

    private final Context context;


    public OmdbHelper(Context context) {
        this.context = context;
    }

    public String getSearchPhraseUrlString(String searchValue, int pageValue)
    {
        // example: http://www.omdbapi.com/?s=sunday&r=json&page=1
        // --------------------------------------------------------------------
        final String scheme = context.getResources().getString(R.string.scheme);
        final String authority = context.getResources().getString(R.string.authority);
        final String searchKey = context.getResources().getString(R.string.omdb_search_key);
        final String dataTypeKey = context.getResources().getString(R.string.omdb_data_type_key);
        final String dataTypeValue = context.getResources().getString(R.string.omdb_data_type_value);
        final String pageKey = context.getResources().getString(R.string.omdb_page_key);

        Uri.Builder builder = new Uri.Builder();
        builder.scheme(scheme)
                .authority(authority)
                .appendQueryParameter(searchKey, searchValue)
                .appendQueryParameter(dataTypeKey, dataTypeValue)
                .appendQueryParameter(pageKey, "" + pageValue);

        return builder.build().toString();
    }

    public String getDetailsUrlString(String searchTitleValue) {

        // example: http://www.omdbapi.com/?t=Matrix&y=&plot=full&r=json
        // --------------------------------------------------------------------
        final String scheme = context.getResources().getString(R.string.scheme);
        final String authority = context.getResources().getString(R.string.authority);
        final String searcTitleKey = context.getResources().getString(R.string.omdb_search_title_key);
        final String plotKey = context.getResources().getString(R.string.omdb_plot_key);
        final String plotValue = context.getResources().getString(R.string.omdb_plot_value);
        final String dataTypeKey = context.getResources().getString(R.string.omdb_data_type_key);
        final String dataTypeValue = context.getResources().getString(R.string.omdb_data_type_value);

        Uri.Builder builder = new Uri.Builder();
        builder.scheme(scheme)
                .authority(authority)
                .appendQueryParameter(searcTitleKey, searchTitleValue)
                .appendQueryParameter(plotKey, plotValue)
                .appendQueryParameter(dataTypeKey, dataTypeValue);

        return builder.build().toString();
    }

    public int GetTotalResult(String jsonString) {

        int totalResult = -1;

        try {

            final String TOTAL_RESULTS_NAME = context.getResources().getString(R.string.omdb_res_total_results_field);

            JSONObject searchObj = new JSONObject(jsonString);

            if ( searchObj.has(TOTAL_RESULTS_NAME))
             totalResult = searchObj.getInt(TOTAL_RESULTS_NAME);

        } catch (JSONException e) {
            Log.e(WebSearchActivity.LOG_CAT, e.getMessage());
        }

        return totalResult;
    }

    public ArrayList<Movie> GetMoviesTitleOnly(String jsonString)
    {
        ArrayList<Movie> list = new ArrayList<>();

        try {

            final String MAIN_OBJECT_NAME = context.getResources().getString(R.string.omdb_res_main_obj);
            final String TITLE_NAME = context.getResources().getString(R.string.omdb_res_title_field);
            final String RESPONSE_NAME = context.getResources().getString(R.string.omdb_res_response_field);

            JSONObject searchObj = new JSONObject(jsonString);

            if (searchObj.has(RESPONSE_NAME) && !searchObj.getBoolean(RESPONSE_NAME))
                return null;

            JSONArray array = searchObj.getJSONArray(MAIN_OBJECT_NAME);
            for (int i=0; i< array.length(); i++)
            {
                JSONObject user = array.getJSONObject(i);

                if (user.has(TITLE_NAME))
                    list.add( new Movie(user.getString(TITLE_NAME)));
            }

        } catch (JSONException e) {
            Log.e(WebSearchActivity.LOG_CAT, e.getMessage());
        }

        return list;
    }

    public Movie GetMovieDetails(String jsonString) {

        Movie movie = null;

        try {
            final String TITLE_NAME = context.getResources().getString(R.string.omdb_res_title_field);
            final String PLOT_NAME = context.getResources().getString(R.string.omdb_res_plot_field);
            final String POSTER_NAME = context.getResources().getString(R.string.omdb_res_poster_field);
            final String IMDBID_NAME = context.getResources().getString(R.string.omdb_res_imdbid_field);

            JSONObject searchObj = new JSONObject(jsonString);

            String subject = searchObj.has(TITLE_NAME) ? searchObj.getString(TITLE_NAME) : "";
            String body = searchObj.has(PLOT_NAME) ? searchObj.getString(PLOT_NAME) : "";
            String posterUrl = searchObj.has(POSTER_NAME) ? searchObj.getString(POSTER_NAME) : "";
            String imdbid = searchObj.has(IMDBID_NAME) ? searchObj.getString(IMDBID_NAME) : "";

            movie = new Movie(subject, body, posterUrl, imdbid);

        } catch (JSONException e) {
            Log.e(WebSearchActivity.LOG_CAT, e.getMessage());
        }

        return movie;
    }
}
