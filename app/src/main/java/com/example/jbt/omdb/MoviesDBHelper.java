package com.example.jbt.omdb;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;


public class MoviesDBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "movies.db";
    private static final int DATABASE_VERSION = 1;

    private static final String SEARCH_TABLE_NAME = "search";
    private static final String COLUMN_SUBJECT = "subject";


    public MoviesDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String SqlStatement = "CREATE TABLE " + SEARCH_TABLE_NAME + "(" +
                COLUMN_SUBJECT + " " + "TEXT PRIMARY KEY);";

        db.execSQL(SqlStatement);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public int bulkInsertSearchResults(Movie[] movies) {

        int returnCount = 0;
        ContentValues[] values = new ContentValues[movies.length];

        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();

        try {
            for (int i=0; i< movies.length; i++)
            {
                values[i] = new ContentValues();
                values[i].put(COLUMN_SUBJECT, movies[i].getSubject());

                long _id = db.insert(SEARCH_TABLE_NAME, null, values[i]);
                if (_id != -1) {
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

        String sqlQuery = "SELECT * FROM " + SEARCH_TABLE_NAME;

        Cursor c = db.rawQuery(sqlQuery, null);
        while(c.moveToNext())
            movies.add(new Movie(c.getString(c.getColumnIndex(COLUMN_SUBJECT))));

        db.close();
        return movies;

    }
}
