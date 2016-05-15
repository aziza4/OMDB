package com.example.jbt.omdb;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private MovieAdapter mAdapter;
    private MoviesDBHelper mDbHelper;

    private Button mAddBtn;
    private ListView mListView;

    public static final String LOG_CAT = "OMDB:";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAddBtn = (Button)findViewById(R.id.addButton);
        mListView = (ListView)findViewById(R.id.mainListView);

        mDbHelper = new MoviesDBHelper(this);
        mDbHelper.deleteAllSearchResult();

        mAdapter = new MovieAdapter(this, mDbHelper.GetDetailsMovieCursor(), 0);
        mListView.setAdapter(mAdapter);

        mAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String[] items = getResources().getStringArray(R.array.add_menu_items);
                final String title = getResources().getString(R.string.add_dialog_title);

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                builder.setTitle(title);
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {

                        if (item == 0 ) // web
                        {
                            Intent intent = new Intent(MainActivity.this, WebSearchActivity.class);
                            startActivity(intent);

                        } else { // manual

                            Movie movie = new Movie("");
                            Intent intent = new Intent(MainActivity.this, EditActivity.class);
                            intent.putExtra(WebSearchActivity.INTENT_MOVIE_KEY, movie);
                            startActivity(intent);
                        }
                    }
                });

                builder.create().show();
            }
        });

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Cursor c = (Cursor) parent.getItemAtPosition(position);
                if (c != null) {
                    Movie movie = Utility.getMovieFromCursor(c);
                    launchEditActivity(movie);
                }
            }
        });


        mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        mListView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {

            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                MenuInflater inflater = mode.getMenuInflater();
                inflater.inflate(R.menu.main_context_menu, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

                SparseBooleanArray checked = mListView.getCheckedItemPositions();

                if (!checked.valueAt(0))
                    return false;

                int position = checked.keyAt(0);

                Cursor c = mAdapter.getCursor();

                if (c == null)
                    return false;

                c.moveToPosition(position);

                Movie movie = Utility.getMovieFromCursor(c);

                switch (item.getItemId())
                {
                    case R.id.editMenuItem:
                        launchEditActivity(movie);
                        mode.finish();
                        return true;

                    case R.id.deleteMenuItem:
                        if ( mDbHelper.deleteMovie(movie.getId())) {
                            String movieDeletedMsg = getResources().getString(R.string.movie_deleted_msg);
                            Toast.makeText(MainActivity.this, movieDeletedMsg, Toast.LENGTH_SHORT).show();
                            RefreshMainList();
                            mode.finish();
                        }
                        return true;

                    default:
                        return false;
                }
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {

            }
        });
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

            case R.id.deleteAllMenuItem:
                ShowDeleteConfirmationDialog();
                return true;

            case R.id.exitMenuItem:
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        RefreshMainList();
    }


    private void ShowDeleteConfirmationDialog() {

        final Resources r = getResources();
        final String deleteTitle = r.getString(R.string.delete_all_title);
        final String deleteMsg = r.getString(R.string.delete_all_message);
        final String deleteButton = r.getString(R.string.delete_all_delete_button);
        final String cancelButton = r.getString(R.string.delete_all_Cancel_button);
        final String deleteAllConfMsg = r.getString(R.string.all_movie_deleted_msg);

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
                                    RefreshMainList();
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


    private void launchEditActivity(Movie movie)
    {
        Intent intent = new Intent(MainActivity.this, EditActivity.class);
        intent.putExtra(WebSearchActivity.INTENT_MOVIE_KEY, movie);
        startActivity(intent);
    }


    private void RefreshMainList() {
        mAdapter.changeCursor(mDbHelper.GetDetailsMovieCursor());
    }
}
