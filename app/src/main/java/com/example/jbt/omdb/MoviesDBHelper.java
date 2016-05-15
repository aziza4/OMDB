package com.example.jbt.omdb;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;

import java.util.ArrayList;


public class MoviesDBHelper extends SQLiteOpenHelper {

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
    public static final String DETAILS_COL_IMAGE = "image";



    public MoviesDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String createSearchTable = String.format(
                "CREATE TABLE %s (%s INTEGER PRIMARY KEY AUTOINCREMENT, %s TEXT);",
                SEARCH_TABLE_NAME, SEARCH_COL_ID, SEARCH_COL_SUBJECT);

        db.execSQL(createSearchTable);

        String createDetailsTable = String.format(
                "CREATE TABLE %s (%s INTEGER PRIMARY KEY AUTOINCREMENT, %s TEXT NOT NULL, " +
                "%s TEXT, %s TEXT, %s TEXT, %s BLOB);",
                DETAILS_TABLE_NAME, DETAILS_COL_ID, DETAILS_COL_SUBJECT,
                DETAILS_COL_BODY, DETAILS_COL_URL, DETAILS_COL_IMDBID, DETAILS_COL_IMAGE);

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
        SQLiteDatabase db = getReadableDatabase();
        String sqlQuery = "SELECT * FROM " + DETAILS_TABLE_NAME + " ORDER BY " + DETAILS_COL_SUBJECT + ";";
        return db.rawQuery(sqlQuery, null);
    }

    public boolean updateOrInsertMoview(Movie movie)
    {
        if (movie.getId() > 0 )
            return updateMovie(movie);

        return insertMovie(movie);
    }

    public boolean insertMovie(Movie movie) {

        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DETAILS_COL_SUBJECT, movie.getSubject());
        values.put(DETAILS_COL_BODY, movie.getBody());
        values.put(DETAILS_COL_URL, movie.getUrl());
        values.put(DETAILS_COL_IMDBID, movie.getImdbId());
        values.put(DETAILS_COL_IMAGE, Utility.convertBitmapToByteArray(movie.getImage()));

        long rowId = db.insert(DETAILS_TABLE_NAME, null, values);
        db.close();

        return rowId > 0;
    }


    public boolean updateMovie(Movie movie) {

        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DETAILS_COL_SUBJECT, movie.getSubject());
        values.put(DETAILS_COL_BODY, movie.getBody());
        values.put(DETAILS_COL_URL, movie.getUrl());
        values.put(DETAILS_COL_IMDBID, movie.getImdbId());
        values.put(DETAILS_COL_IMAGE, Utility.convertBitmapToByteArray(movie.getImage()));

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

    public Movie GetMovie(long id) {

        SQLiteDatabase db = getReadableDatabase();

        String sqlQuery = "SELECT * FROM " + DETAILS_TABLE_NAME + " WHERE " + DETAILS_COL_ID + "=" + id;
        Cursor c = db.rawQuery(sqlQuery, null);

        c.moveToNext();

        long _id = c.getInt(c.getColumnIndex(DETAILS_COL_ID));
        String subject = c.getString( c.getColumnIndex(DETAILS_COL_SUBJECT) );
        String body = c.getString( c.getColumnIndex(DETAILS_COL_BODY) );
        String url = c.getString( c.getColumnIndex(DETAILS_COL_URL) );
        String imdbid = c.getString( c.getColumnIndex(DETAILS_COL_IMDBID) );
        Bitmap image = Utility.convertByteArrayToBitmap(c.getBlob( c.getColumnIndex(DETAILS_COL_IMAGE)));

        c.close();
        db.close();
        return new Movie(_id, subject, body, url, imdbid, image);
    }

    /*
    public ArrayList<Movie> GetAllMovies(long id) {

        ArrayList<Movie> movies = new ArrayList<>();

        SQLiteDatabase db = getReadableDatabase();

        String sqlQuery = "SELECT * FROM " + DETAILS_TABLE_NAME + ";";
        Cursor c = db.rawQuery(sqlQuery, null);

        while(c.moveToNext()) {

            long _id = c.getInt(c.getColumnIndex(DETAILS_COL_ID));
            String subject = c.getString( c.getColumnIndex(DETAILS_COL_SUBJECT) );
            String body = c.getString( c.getColumnIndex(DETAILS_COL_BODY) );
            String url = c.getString( c.getColumnIndex(DETAILS_COL_URL) );
            String imdbid = c.getString( c.getColumnIndex(DETAILS_COL_IMDBID) );
            Bitmap image = Utility.convertByteArrayToBitmap(c.getBlob( c.getColumnIndex(DETAILS_COL_IMAGE)));

            movies.add(new Movie(_id,subject, body, url, imdbid, image));
        }

        db.close();
        return movies;
    }
    */
}
