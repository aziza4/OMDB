package com.example.jbt.omdb;

import android.content.Context;
import android.graphics.Bitmap;


public class Movie {

    private final static long NOT_IN_DB = -1L; // -1 signals this movie is "not yet save in db"

    private long mId;
    private final String mSubject;
    private final String mBody;
    private final String mUrl;
    private final String mImdbId;
    private final float mRating;
    private byte[] mImageBytes; // Internally image is stored as byte[]. However, Bitmap getter/setter are provided as well

    public byte[] getImageByteArray() {
        return mImageBytes;
    }

    public Movie(long _id, String subject, String body, String url, String imdbId, float rating, byte[] imageBytes) {
        this(subject, body, url, imdbId, rating, null);
        mId = _id;
        mImageBytes = imageBytes;
    }

    public Movie(long _id, String subject, String body, String url, String imdbId, float rating, Bitmap image) {
        this(subject, body, url, imdbId, rating, image);
        mId = _id;
    }

    public Movie(String subject, String body, String url, String imdbId, float rating, Bitmap image) {
        mId = NOT_IN_DB;
        mSubject = subject;
        mBody = body;
        mUrl = url;
        mImdbId = imdbId;
        mRating = rating;
        mImageBytes = image == null ? null : ImageHelper.convertBitmapToByteArray(image);
    }

    public Movie(String subject) {
        this(subject, "", "", "", 0f, null);
    }

    public Movie() {
        this("");
    }

    public long getId() {
        return mId;
    }

    public String getSubject() {
        return mSubject;
    }

    public String getBody() {
        return mBody;
    }

    public String getUrl() {
        return mUrl;
    }

    public String getImdbId() {
        return mImdbId;
    }

    public float getRating() {
        return mRating;
    }

    public Bitmap getImage() {

        return mImageBytes == null ?
                null :
                ImageHelper.convertByteArrayToBitmap(mImageBytes);
    }

    @Override
    public String toString() {
        return mSubject;
    }

    public String getDetailsAsText(Context context) // format movie as a string for share-intent
    {
        String subjectTitle = context.getString(R.string.omdb_res_title_field);
        String bodyTitle = context.getString(R.string.omdb_res_plot_field);
        String urlTitle = context.getString(R.string.omdb_res_poster_field);
        String imdbTitle = context.getString(R.string.omdb_res_imdbid_field);
        String ratingTitle = context.getString(R.string.omdb_res_rating_field);

        return String.format("%s: %s\n\n%s: %s\n\n%s: %s\n\n%s: %s\n\n%s: %s\n",
                subjectTitle, mSubject, bodyTitle, mBody, imdbTitle,
                mImdbId, urlTitle, mUrl, ratingTitle, mRating);
    }
}
