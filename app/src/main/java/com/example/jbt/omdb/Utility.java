package com.example.jbt.omdb;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Patterns;

import java.io.ByteArrayOutputStream;
import java.net.MalformedURLException;
import java.net.URL;


public class Utility {

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
        Bitmap image = Utility.convertByteArrayToBitmap(
                c.getBlob( c.getColumnIndex(MoviesDBHelper.DETAILS_COL_IMAGE)));

        return new Movie(_id, subject, body, url, imdbid, image);
    }

    public static boolean isValidUrl(String urlString)
    {
        return Patterns.WEB_URL.matcher(urlString).matches();
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

        Bitmap bitmap =  BitmapFactory.decodeByteArray(byteArray , 0, byteArray.length);
        return bitmap;
    }
}
