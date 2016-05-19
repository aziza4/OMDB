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
    public static final String DETAILS_COL_ID = "_id";
    public static final String DETAILS_COL_SUBJECT = "subject";
    public static final String DETAILS_COL_BODY = "body";
    public static final String DETAILS_COL_URL = "url";
    public static final String DETAILS_COL_IMDBID = "imdbid";
    public static final String DETAILS_COL_RATING = "rating";
    public static final String DETAILS_COL_IMAGE = "image";

    private final Context mContext;

    public MoviesDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String createSearchTable = String.format(
                "CREATE TABLE %s (%s INTEGER PRIMARY KEY AUTOINCREMENT, %s TEXT);",
                SEARCH_TABLE_NAME, SEARCH_COL_ID, SEARCH_COL_SUBJECT);

        db.execSQL(createSearchTable);

        String createDetailsTable = String.format(
                "CREATE TABLE %s (%s INTEGER PRIMARY KEY AUTOINCREMENT, %s TEXT NOT NULL, " +
                "%s TEXT, %s TEXT, %s TEXT, %s REAL, %s BLOB);",
                DETAILS_TABLE_NAME, DETAILS_COL_ID, DETAILS_COL_SUBJECT,
                DETAILS_COL_BODY, DETAILS_COL_URL, DETAILS_COL_IMDBID,
                DETAILS_COL_RATING, DETAILS_COL_IMAGE);

        db.execSQL(createDetailsTable);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }



    // ============================= Search table operations =============================

    public int bulkInsertSearchResults(Movie[] movies) {

        int returnCount = 0;
        ContentValues[] values = new ContentValues[movies.length];

        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();

        try {
            for (int i=0; i< movies.length; i++)
            {
                values[i] = new ContentValues();
                values[i].put(SEARCH_COL_SUBJECT, movies[i].getSubject());

                long rowId = db.insert(SEARCH_TABLE_NAME, null, values[i]);
                if (rowId != -1) {
                    returnCount++;
                }
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

    public Cursor GetDetailsMovieCursor()
    {
        String sortBy, sortOrder;

        SQLiteDatabase db = getReadableDatabase();

        if (Utility.isSortByTitle(mContext)) {

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

    public boolean updateOrInsertMovie(Movie movie)
    {
        if (movie.getId() > 0 )
            return updateMovie(movie);

        return insertMovie(movie);
    }

    private boolean insertMovie(Movie movie) {

        SQLiteDatabase db = getWritableDatabase();

        boolean saveImage = Utility.isSaveImagesToDB(mContext);

        ContentValues values = new ContentValues();
        values.put(DETAILS_COL_SUBJECT, movie.getSubject());
        values.put(DETAILS_COL_BODY, movie.getBody());
        values.put(DETAILS_COL_URL, movie.getUrl());
        values.put(DETAILS_COL_IMDBID, movie.getImdbId());
        values.put(DETAILS_COL_RATING, movie.getRating());
        values.put(DETAILS_COL_IMAGE, saveImage ? movie.getImageByteArray() : null);

        long rowId = db.insert(DETAILS_TABLE_NAME, null, values);
        db.close();

        return rowId > 0;
    }


    private boolean updateMovie(Movie movie) {

        SQLiteDatabase db = getWritableDatabase();

        boolean saveImage = Utility.isSaveImagesToDB(mContext);

        ContentValues values = new ContentValues();
        values.put(DETAILS_COL_SUBJECT, movie.getSubject());
        values.put(DETAILS_COL_BODY, movie.getBody());
        values.put(DETAILS_COL_URL, movie.getUrl());
        values.put(DETAILS_COL_IMDBID, movie.getImdbId());
        values.put(DETAILS_COL_RATING, movie.getRating());
        values.put(DETAILS_COL_IMAGE, saveImage ? movie.getImageByteArray() : null);

        long rowsAffected = db.update(DETAILS_TABLE_NAME, values, DETAILS_COL_ID + "=" + movie.getId(), null);
        db.close();

        return rowsAffected > 0;
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
