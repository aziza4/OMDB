package com.example.jbt.omdb;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
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

import java.util.ArrayList;


public class MainFragment extends Fragment {

    private static final int LOADER_ID = 1;

    private MovieRecyclerAdapter mAdapter;
    private MoviesDBHelper mDbHelper;
    private FragmentHelper mFragmentHelper;
    private OnMainFragListener mListener;

    public MainFragment() {}
    public void onMovieSaved() { refreshMainFrag(); }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mListener = (OnMainFragListener) context;
    }


    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


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

        SharedPrefHelper sharedPrefHelper = new SharedPrefHelper(getActivity());
        boolean mIsTabletMode = sharedPrefHelper.getTabletMode();

        mDbHelper = new MoviesDBHelper(getActivity());
        mDbHelper.deleteAllSearchResult();
        mFragmentHelper = new FragmentHelper(getActivity(), mIsTabletMode);

        mAdapter = new MovieRecyclerAdapter(
                getActivity(),
                mListener,
                mIsTabletMode);

        recyclerView.setAdapter(mAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        getActivity().getSupportLoaderManager().initLoader(LOADER_ID, null, new LoaderManager.LoaderCallbacks<ArrayList<Movie>>() {
            @Override
            public Loader<ArrayList<Movie>> onCreateLoader(int id, Bundle args) {
                return new AsyncTaskLoader<ArrayList<Movie>>(getActivity()) {
                    @Override
                    public ArrayList<Movie> loadInBackground() {
                        return mDbHelper.getDetailsMovieArrayList();
                    }
                };
            }

            @Override
            public void onLoadFinished(Loader<ArrayList<Movie>> loader, ArrayList<Movie> data) {
                mAdapter.setData(data);
            }

            @Override
            public void onLoaderReset(Loader<ArrayList<Movie>> loader) {
                mAdapter.clearData();
            }
        });

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
                                    mListener.onMovieEdit(new Movie());
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
        refreshMainFrag();
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
                                    mAdapter.clearData();
                                    Toast.makeText(getActivity(), deleteAllConfMsg, Toast.LENGTH_SHORT).show();
                                    mFragmentHelper.onMovieDelete();
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


    private void refreshMainFrag() {
        mAdapter.setData(mDbHelper.getDetailsMovieArrayList());
    }


    public interface OnMainFragListener { void onMovieEdit(Movie movie); }
}
