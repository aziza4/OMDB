package com.example.jbt.omdb;


import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SearchView;

import java.net.URL;
import java.util.ArrayList;


public class WebSearchFragment extends Fragment {

    private boolean mIsTabletMode;

    private ArrayAdapter<Movie> mAdapter;
    private OmdbSearchAsyncTask mOmdbSearchAsyncTask;

    private SearchView mSearchSV;
    private ProgressDialog mProgDialog;

    public WebSearchFragment() { }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        SharedPrefHelper sharedPrefHelper = new SharedPrefHelper(getActivity());
        mIsTabletMode = sharedPrefHelper.getTabletMode();

        View viewRoot = inflater.inflate(R.layout.fragment_web_search, container, false);

        mSearchSV = (SearchView) viewRoot.findViewById(R.id.searchSearchView);

        Button cancelBtn = (Button) viewRoot.findViewById(R.id.cancelButton);
        ListView list = (ListView) viewRoot.findViewById(R.id.moviesListView);

        if(list == null || cancelBtn == null)
            return viewRoot;

        mAdapter = new ArrayAdapter<>(getActivity(), R.layout.search_list_item);
        list.setAdapter(mAdapter);

        mSearchSV.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {

                if (query.isEmpty())
                    return false;

                mOmdbSearchAsyncTask = new OmdbSearchAsyncTask();
                mOmdbSearchAsyncTask.execute(query);  // start search async task
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });

        if (mIsTabletMode)
            cancelBtn.setVisibility(View.GONE);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                String searchTitle = mAdapter.getItem(position).toString();

                if (!searchTitle.isEmpty())
                    new OmdbDetailedAsyncTask().execute(searchTitle);  // start get details async task
            }
        });

        return viewRoot;
    }


    @Override
    public void onResume() { // support list availability on various cases, including device orientation
        super.onResume();

        MoviesDBHelper dbHelper = new MoviesDBHelper(getActivity());
        ArrayList<Movie> list = dbHelper.getAllSearchResults();
        RefreshSearchList(list);
        Utility.hideKeyboard(getActivity()); // stop irritating auto keyboard popup
    }


    private void RefreshSearchList(ArrayList<Movie> list)
    {
        mAdapter.clear();
        mAdapter.addAll(list);
    }


    private class OmdbSearchAsyncTask extends AsyncTask<String, Integer, ArrayList<Movie>>
    {
        private boolean mCancelRequested = false; // allow user to request cancellation during download
        private int mTotalResults;

        private ArrayList<Movie> GetNextPageFromOMDB(URL url)
        {
            NetworkHelper networkHelper = new NetworkHelper(url);
            String jsonString = networkHelper.GetJsonString();

            if (jsonString == null)
                return null;

            OmdbHelper omdbHelper = new OmdbHelper(getActivity());
            int totalRes = omdbHelper.GetTotalResult(jsonString);

            if (mTotalResults == 0 && totalRes > 0 )
                mTotalResults = totalRes;

            return omdbHelper.GetMoviesTitlesOnly(jsonString);
        }

        @Override
        protected void onPreExecute() {

            MoviesDBHelper dbHelper = new MoviesDBHelper(getActivity());
            dbHelper.deleteAllSearchResult();

            // Restricts device orientation during download not to lose results on device rotation
            // However, I do provide "show Results" button as escape alternative - see listener below
            Utility.RestrictDeviceOrientation(getActivity());

            String msg =  getString(R.string.progress_bar_message);
            String enoughString = getString(R.string.enough_button);
            mProgDialog = new ProgressDialog(getActivity());
            mProgDialog.setMessage(msg);
            mProgDialog.setIndeterminate(false);
            mProgDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mProgDialog.setCancelable(false);

            mProgDialog.setButton(DialogInterface.BUTTON_NEGATIVE, enoughString, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mOmdbSearchAsyncTask.setCancelRequested(); // user cancels download
                    dialog.dismiss();
                }
            });

            mProgDialog.show();
        }

        @Override
        protected void onPostExecute(ArrayList<Movie> list) {

            if( list != null ) {
                MoviesDBHelper dbHelper = new MoviesDBHelper(getActivity());
                dbHelper.bulkInsertSearchResults(list.toArray(new Movie[list.size()])); // bulk insert
                RefreshSearchList(list);
            }

            Utility.ReleaseDeviceOrientationRestriction(getActivity());
            mSearchSV.setQuery("",false); // for user convenience
            mProgDialog.dismiss();
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            mProgDialog.setMax(progress[0]);
            mProgDialog.setProgress(progress[1]);
        }

        @Override
        protected ArrayList<Movie> doInBackground(String... params) {

            String searchPhrase = params[0];
            OmdbHelper omdbHelper = new OmdbHelper(getActivity());

            mTotalResults = 0;
            ArrayList<Movie> all = new ArrayList<>();
            ArrayList<Movie> page = new ArrayList<>();

            for(int pageNum = 1; page != null && !mCancelRequested; pageNum++) {

                URL url = omdbHelper.GetSearchURL(searchPhrase, pageNum);
                page = GetNextPageFromOMDB(url);

                if (page != null) { // page == null is either download error or last page available
                    all.addAll(page);
                    publishProgress(mTotalResults, all.size());
                }
            }

            return all;
        }

        public void setCancelRequested() { // responds to progress bar "Show Results" button
            mCancelRequested = true;
        }
    }


    private class OmdbDetailedAsyncTask extends AsyncTask<String, Void, Movie> {

        @Override
        protected Movie doInBackground(String... params) {

            Movie movie = null;
            OmdbHelper omdbHelper = new OmdbHelper(getActivity());

            String movieTitle = params[0];
            URL url = omdbHelper.GetDetailsURL(movieTitle);

            NetworkHelper networkHelper = new NetworkHelper(url);
            String jsonString = networkHelper.GetJsonString();

            if (jsonString != null)
                movie = omdbHelper.GetMovieDetails(jsonString); // let omdbHelper do the dirty work...

            return movie;
        }

        @Override
        protected void onPostExecute(Movie movie) {

            if (movie == null)
                return;

            MoviesDBHelper dbHelper = new MoviesDBHelper(getActivity());
            dbHelper.updateOrInsertEditMovie(movie);

            FragmentHelper fragmentHelper = new FragmentHelper(getActivity(), mIsTabletMode);
            dbHelper.updateOrInsertEditMovie(movie);
            fragmentHelper.launchEditOperation();
        }
    }
}
