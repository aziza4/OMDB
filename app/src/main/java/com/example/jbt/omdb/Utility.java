package com.example.jbt.omdb;


import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;


class Utility {

    // disable device rotation during search titles download to avoid data lost...
    // however, user has the option to abort download anytime with a button click.
    public static void RestrictDeviceOrientation(Activity activity) {
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


    public static void hideKeyboard(Activity activity) {
        activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }


    // Workaround android bug: http://stackoverflow.com/questions/22884068/troubles-with-activity-title-language
    private static void resetTitle(AppCompatActivity activity, int id)
    {
        ActionBar actionBar = activity.getSupportActionBar();

        if (actionBar != null)
            actionBar.setTitle(activity.getString(id));
    }


    public static void setContentViewWithLocaleChange(AppCompatActivity activity, int layoutId, int titleId)
    {
        SharedPrefHelper sharedPrefHelper = new SharedPrefHelper(activity);
        sharedPrefHelper.changeLocale();
        activity.setContentView(layoutId);
        resetTitle(activity, titleId); // workaround android bug, see above
    }
}

