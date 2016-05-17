package com.example.jbt.omdb;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;


public class Movie implements Parcelable {

    private final static long NOT_IN_DB = -1;

    private long mId;
    private final String mSubject;
    private final String mBody;
    private final String mUrl;
    private final String mImdbId;
    private byte[] mImageBytes;

    public byte[] getImageByteArray() {
        return mImageBytes;
    }

    public Movie(long _id, String subject, String body, String url, String imdbId, byte[] imageBytes) {
        this(subject, body, url, imdbId, null);
        mId = _id;
        mImageBytes = imageBytes;
    }

    public Movie(long _id, String subject, String body, String url, String imdbId, Bitmap image) {
        this(subject, body, url, imdbId, image);
        mId = _id;
    }

    public Movie(String subject, String body, String url, String imdbId, Bitmap image) {
        mId = NOT_IN_DB;
        mSubject = subject;
        mBody = body;
        mUrl = url;
        mImdbId = imdbId;
        mImageBytes = Utility.convertBitmapToByteArray(image);
    }

    public Movie(String subject) {
        this(subject, "", "", "", null);
    }

    private Movie(Parcel in) {

        mId = in.readLong();
        mSubject = in.readString();
        mBody = in.readString();
        mUrl = in.readString();
        mImdbId = in.readString();

        mImageBytes = new byte[in.readInt()];
        in.readByteArray(mImageBytes);
    }

    public static final Creator<Movie> CREATOR = new Creator<Movie>() {
        @Override
        public Movie createFromParcel(Parcel in) {
            return new Movie(in);
        }

        @Override
        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };

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

    public Bitmap getImage() {

        return mImageBytes == null ?
                null :
                Utility.convertByteArrayToBitmap(mImageBytes);
    }

    @Override
    public String toString() {
        return mSubject;
    }

    public String getDetailsAsText(Context context)
    {
        String subjectTitle = context.getResources().getString(R.string.omdb_res_title_field);
        String bodyTitle = context.getResources().getString(R.string.omdb_res_plot_field);
        String urlTitle = context.getResources().getString(R.string.omdb_res_poster_field);
        String imdbTitle = context.getResources().getString(R.string.omdb_res_imdbid_field);

        return String.format("%s: %s\n\n%s: %s\n\n%s: %s\n\n%s: %s\n\n",
                subjectTitle, mSubject, bodyTitle, mBody, imdbTitle, mImdbId, urlTitle, mUrl);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(mId);
        dest.writeString(mSubject);
        dest.writeString(mBody);
        dest.writeString(mUrl);
        dest.writeString(mImdbId);

        if ( mImageBytes != null) {
            dest.writeInt(mImageBytes.length);
            dest.writeByteArray(mImageBytes);
        }
    }
}
