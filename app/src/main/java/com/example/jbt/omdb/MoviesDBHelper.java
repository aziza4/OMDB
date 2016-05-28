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

    private static final String EDIT_TABLE_NAME = "edit";
    private static final String EDIT_COL_ID = "_id";
    private static final String EDIT_COL_ORIGINAL_ID = "originalid";
    private static final String EDIT_COL_SUBJECT = "subject";
    private static final String EDIT_COL_BODY = "body";
    private static final String EDIT_COL_URL = "url";
    private static final String EDIT_COL_IMDBID = "imdbid";
    private static final String EDIT_COL_RATING = "rating";
    private static final String EDIT_COL_IMAGE = "image";
    private static final long EDIT_COL_ID_CONST = -2;

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

        String createEditTable = String.format( // holds single record only that reflects Edit fragment state at any give time
                "CREATE TABLE %s (%s INTEGER PRIMARY KEY, %s INTEGER, %s TEXT NOT NULL, " +
                        "%s TEXT, %s TEXT, %s TEXT, %s REAL, %s BLOB);",
                EDIT_TABLE_NAME, EDIT_COL_ID, EDIT_COL_ORIGINAL_ID,
                EDIT_COL_SUBJECT, EDIT_COL_BODY, EDIT_COL_URL,
                EDIT_COL_IMDBID, EDIT_COL_RATING, EDIT_COL_IMAGE);

        db.execSQL(createEditTable);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}



    // ============================= Search table operations =============================


    public int bulkInsertSearchResults(Movie[] movies) {

        int returnCount = 0;
        ContentValues[] values = new ContentValues[movies.length];

        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();

        try
        {
            for (int i=0; i< movies.length; i++)
            {
                values[i] = new ContentValues();
                values[i].put(SEARCH_COL_SUBJECT, movies[i].getSubject());

                long rowId = db.insert(SEARCH_TABLE_NAME, null, values[i]);

                if (rowId != -1)
                    returnCount++;
            }

            db.setTransactionSuccessful();

        } finally {
            db.endTransaction();
        }

        db.close();
        return returnCount;
    }


    public int deleteAllSearchResult() {

        SQLiteDatabase db = getWritableDatabase();
        int rowsDeleted = db.delete(SEARCH_TABLE_NAME, null , null);
        db.close();

        return rowsDeleted;
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



// ============================= Edit table operations =============================


    public Movie getEditMovie()
    {
        Movie movie = new Movie();

        SQLiteDatabase db = getReadableDatabase();
        String sqlQuery = "SELECT * FROM " + EDIT_TABLE_NAME + ";";
        Cursor c = db.rawQuery(sqlQuery, null);

        if ( c.moveToNext() ) { // expecting only single row on this table

            long originalId = c.getInt(c.getColumnIndex(EDIT_COL_ORIGINAL_ID));
            String subject = c.getString(c.getColumnIndex(EDIT_COL_SUBJECT));
            String body = c.getString(c.getColumnIndex(EDIT_COL_BODY));
            String url = c.getString(c.getColumnIndex(EDIT_COL_URL));
            String imdbid = c.getString(c.getColumnIndex(EDIT_COL_IMDBID));
            float rating = c.getFloat(c.getColumnIndex(EDIT_COL_RATING));
            byte[] imageBytes = c.getBlob(c.getColumnIndex(EDIT_COL_IMAGE));

            movie = new Movie(originalId, subject, body, url, imdbid, rating, imageBytes);

        } else { // table is empty

            insertEditMovie(movie);
        }

        c.close();
        db.close();

        return movie;
    }


    public boolean updateOrInsertEditMovie(Movie movie) {

        return updateEditMovie(movie) || insertEditMovie(movie);
    }


    private boolean insertEditMovie(Movie movie) {

        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(EDIT_COL_ID, EDIT_COL_ID_CONST);
        values.put(EDIT_COL_ORIGINAL_ID, movie.getId());
        values.put(EDIT_COL_SUBJECT, movie.getSubject());
        values.put(EDIT_COL_BODY, movie.getBody());
        values.put(EDIT_COL_URL, movie.getUrl());
        values.put(EDIT_COL_IMDBID, movie.getImdbId());
        values.put(EDIT_COL_RATING, movie.getRating());
        values.put(EDIT_COL_IMAGE, movie.getImageByteArray());

        long rowId = db.insert(EDIT_TABLE_NAME, null, values);
        db.close();

        return rowId > 0;
    }


    private boolean updateEditMovie(Movie movie) {

        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(EDIT_COL_ORIGINAL_ID, movie.getId());
        values.put(EDIT_COL_SUBJECT, movie.getSubject());
        values.put(EDIT_COL_BODY, movie.getBody());
        values.put(EDIT_COL_URL, movie.getUrl());
        values.put(EDIT_COL_IMDBID, movie.getImdbId());
        values.put(EDIT_COL_RATING, movie.getRating());
        values.put(EDIT_COL_IMAGE, movie.getImageByteArray());

        long rowsAffected = db.update(EDIT_TABLE_NAME, values, EDIT_COL_ID + "=" + EDIT_COL_ID_CONST, null);
        db.close();

        return rowsAffected > 0; // although return val not used, its a good practice (debug)
    }

    public boolean deleteAllEditMovies() {

        SQLiteDatabase db = getWritableDatabase();
        long rowsDeleted = db.delete(EDIT_TABLE_NAME, null , null);
        db.close();

        return rowsDeleted > 0; // although return val not used, its a good practice (debug)
    }
}

