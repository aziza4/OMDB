package com.example.jbt.omdb;

import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;


class OmdbHelper {

    private final String mScheme;
    private final String mAuthority;
    private final String mSearchKey;
    private final String mDataTypeKey;
    private final String mDataTypeValue;
    private final String mPageKey;
    private final String mSearchTitleKey;
    private final String mPlotKey;
    private final String mPlotValue;
    private final String mTotalResult;
    private final String mMainObjName;
    private final String mTitleName;
    private final String mResponseName;
    private final String mPlotName;
    private final String mPosterName;
    private final String mImdbName;
    private final String mNAvalue;


    public OmdbHelper(Context context) {

        Resources resources = context.getResources();

        mScheme = resources.getString(R.string.http_scheme);
        mAuthority = resources.getString(R.string.authority);
        mSearchKey = resources.getString(R.string.omdb_search_key);
        mDataTypeKey = resources.getString(R.string.omdb_data_type_key);
        mDataTypeValue = resources.getString(R.string.omdb_data_type_value);
        mPageKey = resources.getString(R.string.omdb_page_key);
        mSearchTitleKey = resources.getString(R.string.omdb_search_title_key);
        mPlotKey = resources.getString(R.string.omdb_plot_key);
        mPlotValue = resources.getString(R.string.omdb_plot_value);
        mTotalResult = resources.getString(R.string.omdb_res_total_results_field);

        mMainObjName = resources.getString(R.string.omdb_res_main_obj);
        mTitleName = resources.getString(R.string.omdb_res_title_field);
        mResponseName = resources.getString(R.string.omdb_res_response_field);
        mPlotName = resources.getString(R.string.omdb_res_plot_field);
        mPosterName = resources.getString(R.string.omdb_res_poster_field);
        mImdbName = resources.getString(R.string.omdb_res_imdbid_field);
        mNAvalue = resources.getString(R.string.omdb_res_na_value);
    }


    private String getSearchPhraseUrlString(String searchValue, int pageValue)
    {
        // example: http://www.omdbapi.com/?s=sunday&r=json&page=1
        // --------------------------------------------------------------------
        Uri.Builder builder = new Uri.Builder();
        builder.scheme(mScheme)
                .authority(mAuthority)
                .appendQueryParameter(mSearchKey, searchValue)
                .appendQueryParameter(mDataTypeKey, mDataTypeValue)
                .appendQueryParameter(mPageKey, "" + pageValue);

        return builder.build().toString();
    }


    private String getDetailsUrlString(String searchTitleValue) {

        // example: http://www.omdbapi.com/?t=Matrix&y=&plot=full&r=json
        // --------------------------------------------------------------------
        Uri.Builder builder = new Uri.Builder();
        builder.scheme(mScheme)
                .authority(mAuthority)
                .appendQueryParameter(mSearchTitleKey, searchTitleValue)
                .appendQueryParameter(mPlotKey, mPlotValue)
                .appendQueryParameter(mDataTypeKey, mDataTypeValue);

        return builder.build().toString();
    }


    public int GetTotalResult(String jsonString) {

        int totalResult = -1;

        try {

            JSONObject searchObj = new JSONObject(jsonString);

            if ( searchObj.has(mTotalResult))
                totalResult = searchObj.getInt(mTotalResult);

        } catch (JSONException e) {
            Log.e(MainActivity.LOG_CAT, "" + e.getMessage());
        }

        return totalResult;
    }


    public ArrayList<Movie> GetMoviesTitlesOnly(String jsonString)
    {
        ArrayList<Movie> list = new ArrayList<>();

        try {

            JSONObject searchObj = new JSONObject(jsonString);

            if (searchObj.has(mResponseName) && !searchObj.getBoolean(mResponseName))
                return null;

            JSONArray array = searchObj.getJSONArray(mMainObjName);
            for (int i=0; i< array.length(); i++)
            {
                JSONObject user = array.getJSONObject(i);

                String subject = getJsonFieldValue(user, mTitleName);

                if (subject != null && !subject.isEmpty())
                    list.add( new Movie(subject));
            }

        } catch (JSONException e) {
            Log.e(MainActivity.LOG_CAT, "" + e.getMessage());
        }

        return list;
    }

    public Movie GetMovieDetails(String jsonString) {

        Movie movie = null;

        try {

            JSONObject searchObj = new JSONObject(jsonString);

            String subject = getJsonFieldValue(searchObj, mTitleName);
            String body = getJsonFieldValue(searchObj, mPlotName);
            String posterUrl = getJsonFieldValue(searchObj, mPosterName);
            String imdbid = getJsonFieldValue(searchObj, mImdbName);

            movie = new Movie(subject, body, posterUrl, imdbid, null);

        } catch (JSONException e) {
            Log.e(MainActivity.LOG_CAT, "" + e.getMessage());
        }

        return movie;
    }


    private String getJsonFieldValue(JSONObject jsonObj, String fieldName)
    {
        String res = "";

        try {

            if (jsonObj.has(fieldName))
                res = jsonObj.getString(fieldName).trim();

        } catch (JSONException e) {
            Log.e(MainActivity.LOG_CAT, "" + e.getMessage());
        }

        return res.equals(mNAvalue) ? "" : res;
    }


    public URL GetSearchURL(String searchPhrase, int pageNum)
    {
        URL url = null;

        try {

            url =  new URL(getSearchPhraseUrlString(searchPhrase, pageNum));

        } catch (MalformedURLException e) {

            Log.e(MainActivity.LOG_CAT, "" + e.getMessage());
        }

        return url;
    }


    public URL GetDetailsURL(String title)
    {
        URL url = null;

        try {

            url = new URL(getDetailsUrlString(title));

        } catch (MalformedURLException e) {

            Log.e(MainActivity.LOG_CAT, "" + e.getMessage());
        }

        return url;
    }
}
