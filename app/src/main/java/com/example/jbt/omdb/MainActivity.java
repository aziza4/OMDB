package com.example.jbt.omdb;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private MovieRecyclerAdapter mAdapter;
    private MoviesDBHelper mDbHelper;

    public static final String LOG_CAT = "OMDB:";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Utility.changeLocale(this);
        setContentView(R.layout.activity_main);

        resetAppName(); // workaround android bug...

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.mainListView);

        if (recyclerView == null )
            return;

        mDbHelper = new MoviesDBHelper(this);
        mDbHelper.deleteAllSearchResult();

        mAdapter = new MovieRecyclerAdapter(this, mDbHelper.getDetailsMovieArrayList());

        recyclerView.setAdapter(mAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        FloatingActionButton addFab = (FloatingActionButton) findViewById(R.id.addFAB);

        if (addFab != null)
            addFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    final int WEB_OPTION_INDEX = 0;
                    final int MAN_OPTION_INDEX = 1;

                    final String[] items = getResources().getStringArray(R.array.add_menu_items);
                    final String title = getString(R.string.add_dialog_title);

                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                    builder.setTitle(title);
                    builder.setItems(items, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int item) {

                            Intent intent;

                            switch (item)
                            {
                                case WEB_OPTION_INDEX:
                                    intent = new Intent(MainActivity.this, WebSearchActivity.class);
                                    startActivity(intent);
                                    break;

                                case MAN_OPTION_INDEX:
                                    Movie movie = new Movie("");
                                    intent = new Intent(MainActivity.this, EditActivity.class);
                                    intent.putExtra(WebSearchActivity.INTENT_MOVIE_KEY, movie);
                                    startActivity(intent);
                                    break;
                            }
                        }
                    });

                    builder.create().show();
                }
            });
    }

    @Override
    protected void onResume() {
        super.onResume();

        refreshMainList();
        invalidateOptionsMenu();

        if (Utility.wasLocaleChanged(this))
            recreate(); // language change to take immediately affect
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.settingsMenuItem:
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                return true;

            case R.id.deleteAllMenuItem:
                showDeleteConfirmationDialog();
                return true;

            case R.id.exitMenuItem:
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private void showDeleteConfirmationDialog() {

        final String deleteTitle = getString(R.string.delete_all_title);
        final String deleteMsg = getString(R.string.delete_all_message);
        final String deleteButton = getString(R.string.delete_all_delete_button);
        final String cancelButton = getString(R.string.delete_all_Cancel_button);
        final String deleteAllConfMsg = getString(R.string.all_movie_deleted_msg);

        new AlertDialog.Builder(this)
                .setTitle(deleteTitle)
                .setMessage(deleteMsg)
                .setIcon(android.R.drawable.ic_delete)

                .setPositiveButton(deleteButton,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                if ( mDbHelper.deleteAllMovies() )
                                {
                                    Toast.makeText(MainActivity.this, deleteAllConfMsg, Toast.LENGTH_SHORT).show();
                                    refreshMainList();
                                }
                                dialog.dismiss();
                            }
                        })

                .setNegativeButton(cancelButton,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })

                .create()
                .show();
    }

    private void refreshMainList() {
        mAdapter.setData(mDbHelper.getDetailsMovieArrayList());
    }

    private void resetAppName()
    {
        // workaround android bug: http://stackoverflow.com/questions/22884068/troubles-with-activity-title-language
        String appName = getString(R.string.app_name);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setTitle(appName);
    }
}
