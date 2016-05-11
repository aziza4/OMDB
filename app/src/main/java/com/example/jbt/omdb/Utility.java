package com.example.jbt.omdb;


import android.content.Context;
import android.net.Uri;

public class Utility {

    public static String getSearchPhraseUrlString(Context context, String searchValue, int pageValue)
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

    public static String getDetailsUrlString(Context context, String searchTitleValue) {

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
}
