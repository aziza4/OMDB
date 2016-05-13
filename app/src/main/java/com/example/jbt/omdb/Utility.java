package com.example.jbt.omdb;


import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;


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
}
