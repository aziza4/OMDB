package com.example.jbt.omdb;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.database.Cursor;


public class Utility {

    public static void RestricDeviceOrientation(Activity activity)
    {
        int current_orientation = activity.getResources().getConfiguration().orientation;

        int newFixedOrientation =
                current_orientation == Configuration.ORIENTATION_LANDSCAPE ?
                        ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE :
                        ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;

        activity.setRequestedOrientation(newFixedOrientation);
    }

    public static void ReleaseDeviceOrientationRestriction(Activity activity) {

        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
    }

    public static Movie getMovieFromCursor(Cursor c)
    {
        long _id = c.getInt(c.getColumnIndex(MoviesDBHelper.DETAILS_COL_ID));
        String subject = c.getString(c.getColumnIndex(MoviesDBHelper.DETAILS_COL_SUBJECT));
        String body = c.getString( c.getColumnIndex(MoviesDBHelper.DETAILS_COL_BODY) );
        String url = c.getString( c.getColumnIndex(MoviesDBHelper.DETAILS_COL_URL) );
        String imdbid = c.getString( c.getColumnIndex(MoviesDBHelper.DETAILS_COL_IMDBID) );

        return new Movie(_id, subject, body, url, imdbid);
    }
}
