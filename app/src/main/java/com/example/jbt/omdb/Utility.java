package com.example.jbt.omdb;


import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;
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

    public static boolean isSortByTitle(Context context) {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        String key = context.getString(R.string.pref_sort_key);
        String def = context.getString(R.string.pref_sort_title);
        String title = context.getString(R.string.pref_sort_title);

        return prefs.getString(key, def).equals(title);
    }

    public static boolean isSaveImagesToDB(Context context) {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        String key = context.getString(R.string.pref_enable_save_image_key);
        String def = context.getString(R.string.pref_enable_save_image_default);
        return prefs.getBoolean(key, Boolean.parseBoolean(def));
    }
}
