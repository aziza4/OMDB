package com.example.jbt.omdb;


import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.WindowManager;

import java.io.ByteArrayOutputStream;



class Utility {

    public static void RestrictDeviceOrientation(Activity activity)
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
        float rating = c.getFloat( c.getColumnIndex(MoviesDBHelper.DETAILS_COL_RATING) );
        byte[] imageBytes = c.getBlob( c.getColumnIndex(MoviesDBHelper.DETAILS_COL_IMAGE));

        return new Movie(_id, subject, body, url, imdbid, rating, imageBytes);
    }


    public static byte[] convertBitmapToByteArray(Bitmap bitmap)
    {
        if (bitmap == null)
            return null;

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }

    public static Bitmap convertByteArrayToBitmap(byte[] byteArray)
    {
        if (byteArray == null)
            return null;

        return BitmapFactory.decodeByteArray(byteArray , 0, byteArray.length);
    }

    public static void hideKeyboard(Activity activity)
    {
        activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

}
