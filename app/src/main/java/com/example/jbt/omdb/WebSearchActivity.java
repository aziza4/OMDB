package com.example.jbt.omdb;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SearchView;
import java.net.URL;
import java.util.ArrayList;

public class WebSearchActivity extends AppCompatActivity implements EditFragment.OnEditDoneListener {

    private FragmentHelper mFragmentHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utility.setContentViewWithLocaleChange(this, R.layout.activity_web_search, R.string.web_search_name);

        boolean isTabletMode = findViewById(R.id.editFragContainer) != null;
        SharedPrefHelper sharedPrefHelper = new SharedPrefHelper(this);
        sharedPrefHelper.saveTabletMode(isTabletMode);

        mFragmentHelper = new FragmentHelper(this, isTabletMode);
        mFragmentHelper.replaceWebSearchFragment(); // create webSearch fragment
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode != EditFragment.REQUEST_TAKE_PHOTO || resultCode != Activity.RESULT_OK)
            return;

        EditFragment editFrag = mFragmentHelper.getEditFragmentIfExists();

        if (editFrag != null)
            editFrag.onCameraActivityResult();
    }

    @Override
    public void onMovieSaved() { }
}
