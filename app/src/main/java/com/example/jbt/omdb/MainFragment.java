package com.example.jbt.omdb;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;


public class MainFragment extends Fragment {

    private MovieRecyclerAdapter mAdapter;
    private MoviesDBHelper mDbHelper;
    private boolean mIsTabletMode;

    public MainFragment() {}
    public void setTabletMode(boolean isTabletMode) { mIsTabletMode = isTabletMode; }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.mainListView);

        mDbHelper = new MoviesDBHelper(getActivity());
        mDbHelper.deleteAllSearchResult();

        mAdapter = new MovieRecyclerAdapter(getActivity(), mDbHelper.getDetailsMovieArrayList(), mIsTabletMode);

        recyclerView.setAdapter(mAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        FloatingActionButton addFab = (FloatingActionButton) rootView.findViewById(R.id.addFAB);

        if (addFab != null)
            addFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    final int WEB_OPTION_INDEX = 0;
                    final int MAN_OPTION_INDEX = 1;

                    final String[] items = getResources().getStringArray(R.array.add_menu_items);
                    final String title = getString(R.string.add_dialog_title);

                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                    builder.setTitle(title);
                    builder.setItems(items, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int item) {

                            Intent intent;

                            switch (item)
                            {
                                case WEB_OPTION_INDEX:
                                    intent = new Intent(getActivity(), WebSearchActivity.class);
                                    startActivity(intent);
                                    break;

                                case MAN_OPTION_INDEX:
                                    Movie movie = new Movie("");
                                    intent = new Intent(getActivity(), EditActivity.class);
                                    intent.putExtra(WebSearchActivity.INTENT_MOVIE_KEY, movie);
                                    startActivity(intent);
                                    break;
                            }
                        }
                    });

                    builder.create().show();
                }
            });

        return rootView;
    }


    @Override
    public void onResume() {
        super.onResume();
        refreshMainList();
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.options_menu, menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.settingsMenuItem:
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;

            case R.id.deleteAllMenuItem:
                showDeleteConfirmationDialog();
                return true;

            case R.id.exitMenuItem:
                getActivity().finish();
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

        new AlertDialog.Builder(getActivity())
                .setTitle(deleteTitle)
                .setMessage(deleteMsg)
                .setIcon(android.R.drawable.ic_delete)

                .setPositiveButton(deleteButton,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                if ( mDbHelper.deleteAllMovies() )
                                {
                                    Toast.makeText(getActivity(), deleteAllConfMsg, Toast.LENGTH_SHORT).show();
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
}
