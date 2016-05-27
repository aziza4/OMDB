package com.example.jbt.omdb;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import java.util.Locale;

class SharedPrefHelper {

    private final Context mContext;
    private final SharedPreferences mPrefs;

    public SharedPrefHelper(Context context)
    {
        mContext = context;
        mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public boolean isSortByTitle() {

        String key = mContext.getString(R.string.pref_sort_key);
        String def = mContext.getString(R.string.pref_sort_title);
        String title = mContext.getString(R.string.pref_sort_title);

        return mPrefs.getString(key, def).equals(title);
    }

    public boolean isSaveImagesToDB() {

        String key = mContext.getString(R.string.pref_enable_save_image_key);
        String def = mContext.getString(R.string.pref_enable_save_image_default);
        return mPrefs.getBoolean(key, Boolean.parseBoolean(def));
    }


    public void changeLocale() {

        String key = mContext.getString(R.string.pref_lang_key);
        String def = mContext.getString(R.string.pref_lang_english);
        String lang = mPrefs.getString(key, def);

        Locale locale = new Locale(lang);
        Locale.setDefault(locale);

        Configuration configuration = new Configuration();
        configuration.locale = locale;
        Resources resources = mContext.getResources();
        resources.updateConfiguration(configuration, mContext.getResources().getDisplayMetrics());
    }

    public void saveTabletMode(boolean isTabletMode)
    {
        String key = mContext.getString(R.string.pref_is_tablet_key);
        mPrefs.edit().putBoolean(key, isTabletMode).apply();
    }

    public boolean getTabletMode()
    {
        String key = mContext.getString(R.string.pref_is_tablet_key);
        String def = mContext.getString(R.string.pref_is_tablet_default);
        return mPrefs.getBoolean(key,Boolean.parseBoolean(def));
    }
}
