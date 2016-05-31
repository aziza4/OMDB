package com.example.jbt.omdb;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;


class MoviesDBHelper extends SQLiteOpenHelper {


    private static final String DATABASE_NAME = "movies.db";
    private static final int DATABASE_VERSION = 1;

    private static final String SEARCH_TABLE_NAME = "search";
    private static final String SEARCH_COL_ID = "_id";
    private static final String SEARCH_COL_SUBJECT = "subject";

    private static final String DETAILS_TABLE_NAME = "details";
    private static final String DETAILS_COL_ID = "_id";
    private static final String DETAILS_COL_SUBJECT = "subject";
    private static final String DETAILS_COL_BODY = "body";
    private static final String DETAILS_COL_URL = "url";
    private static final String DETAILS_COL_IMDBID = "imdbid";
    private static final String DETAILS_COL_RATING = "rating";
    private static final String DETAILS_COL_IMAGE = "image";

    private final Context mContext;

    public MoviesDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
    }


    @Override
    public void onCreate(SQLiteDatabase db) {

        String createSearchTable = String.format( // holds last web search results
                "CREATE TABLE %s (%s INTEGER PRIMARY KEY AUTOINCREMENT, %s TEXT);",
                SEARCH_TABLE_NAME, SEARCH_COL_ID, SEARCH_COL_SUBJECT);

        db.execSQL(createSearchTable);

        String createDetailsTable = String.format( // holds all user created movies
                "CREATE TABLE %s (%s INTEGER PRIMARY KEY AUTOINCREMENT, %s TEXT NOT NULL, " +
                "%s TEXT, %s TEXT, %s TEXT, %s REAL, %s BLOB);",
                DETAILS_TABLE_NAME, DETAILS_COL_ID, DETAILS_COL_SUBJECT,
                DETAILS_COL_BODY, DETAILS_COL_URL, DETAILS_COL_IMDBID,
                DETAILS_COL_RATING, DETAILS_COL_IMAGE);

        db.execSQL(createDetailsTable);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}



    // ============================= Search table operations =============================


    public void bulkInsertSearchResults(Movie[] movies) {

        ContentValues[] values = new ContentValues[movies.length];

        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();

        try
        {
            for (int i=0; i< movies.length; i++)
            {
                values[i] = new ContentValues();
                values[i].put(SEARCH_COL_SUBJECT, movies[i].getSubject());

                db.insert(SEARCH_TABLE_NAME, null, values[i]);
            }

            db.setTransactionSuccessful();

        } finally {
            db.endTransaction();
        }

        db.close();
    }


    public void deleteAllSearchResult() {

        SQLiteDatabase db = getWritableDatabase();
        db.delete(SEARCH_TABLE_NAME, null , null);
        db.close();
    }


    public ArrayList<Movie> getAllSearchResults()
    {
        ArrayList<Movie> movies = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        String sqlQuery = "SELECT * FROM " + SEARCH_TABLE_NAME + ";";

        Cursor c = db.rawQuery(sqlQuery, null);
        while(c.moveToNext())
            movies.add(new Movie(c.getString(c.getColumnIndex(SEARCH_COL_SUBJECT))));

        c.close();
        db.close();
        return movies;
    }



    // ============================= Details table operations =============================


    private Cursor getDetailsMovieCursor()
    {
        String sortBy, sortOrder;
        SharedPrefHelper sharedPrefHelper = new SharedPrefHelper(mContext);

        SQLiteDatabase db = getReadableDatabase();

        if (sharedPrefHelper.isSortByTitle()) {

            sortBy = DETAILS_COL_SUBJECT;
            sortOrder = "";
        } else {

            sortBy = DETAILS_COL_RATING;
            sortOrder = "DESC";
        }

        String sqlQuery = "SELECT * FROM " + DETAILS_TABLE_NAME +
                " ORDER BY " + sortBy + " " + sortOrder + ";";

        return db.rawQuery(sqlQuery, null);
    }


    public ArrayList<Movie> getDetailsMovieArrayList()
    {
        ArrayList<Movie> movies = new ArrayList<>();

        Cursor c = getDetailsMovieCursor();

        final int id_index = c.getColumnIndex(DETAILS_COL_ID);
        final int id_subject = c.getColumnIndex(DETAILS_COL_SUBJECT);
        final int id_body = c.getColumnIndex(DETAILS_COL_BODY);
        final int id_url = c.getColumnIndex(DETAILS_COL_URL);
        final int id_imdbid = c.getColumnIndex(DETAILS_COL_IMDBID);
        final int id_rating = c.getColumnIndex(DETAILS_COL_RATING);
        final int id_image = c.getColumnIndex(DETAILS_COL_IMAGE);

        while(c.moveToNext()) {

            long _id = c.getInt(id_index);
            String subject = c.getString(id_subject);
            String body = c.getString(id_body);
            String url = c.getString(id_url);
            String imdbid = c.getString(id_imdbid);
            float rating = c.getFloat(id_rating);
            byte[] imageBytes = c.getBlob(id_image);

            movies.add(new Movie(_id,subject, body, url, imdbid, rating, imageBytes));
        }

        c.close();
        return movies;

    }


    public boolean updateOrInsertMovie(Movie movie)
    {
        return movie.getId() > 0 ? // movie that was already saved in db has an id >= 1
                updateMovie(movie) :
                insertMovie(movie);
    }


    private boolean insertMovie(Movie movie) {

        SQLiteDatabase db = getWritableDatabase();

        SharedPrefHelper sharedPrefHelper = new SharedPrefHelper(mContext);
        boolean saveImage = sharedPrefHelper.isSaveImagesToDB();

        ContentValues values = new ContentValues();
        values.put(DETAILS_COL_SUBJECT, movie.getSubject());
        values.put(DETAILS_COL_BODY, movie.getBody());
        values.put(DETAILS_COL_URL, movie.getUrl());
        values.put(DETAILS_COL_IMDBID, movie.getImdbId());
        values.put(DETAILS_COL_RATING, movie.getRating());
        values.put(DETAILS_COL_IMAGE, saveImage ? movie.getImageByteArray() : null);

        long rowId = db.insert(DETAILS_TABLE_NAME, null, values);
        db.close();

        return rowId > 0; // although return val not used, its a good practice (debug)
    }


    private boolean updateMovie(Movie movie) {

        SQLiteDatabase db = getWritableDatabase();

        SharedPrefHelper sharedPrefHelper = new SharedPrefHelper(mContext);
        boolean saveImage = sharedPrefHelper.isSaveImagesToDB();

        ContentValues values = new ContentValues();
        values.put(DETAILS_COL_SUBJECT, movie.getSubject());
        values.put(DETAILS_COL_BODY, movie.getBody());
        values.put(DETAILS_COL_URL, movie.getUrl());
        values.put(DETAILS_COL_IMDBID, movie.getImdbId());
        values.put(DETAILS_COL_RATING, movie.getRating());
        values.put(DETAILS_COL_IMAGE, saveImage ? movie.getImageByteArray() : null);

        long rowsAffected = db.update(DETAILS_TABLE_NAME, values, DETAILS_COL_ID + "=" + movie.getId(), null);
        db.close();

        return rowsAffected > 0; // although return val not used, its a good practice (debug)
    }


    public boolean deleteMovie(long id) {

        SQLiteDatabase db = getWritableDatabase();
        long rowsDeleted = db.delete(DETAILS_TABLE_NAME, DETAILS_COL_ID + " =" +  id , null);
        db.close();

        return rowsDeleted > 0;
    }


    public boolean deleteAllMovies() {

        SQLiteDatabase db = getWritableDatabase();
        long rowsDeleted = db.delete(DETAILS_TABLE_NAME, null , null);
        db.close();

        return rowsDeleted > 0;
    }
}

