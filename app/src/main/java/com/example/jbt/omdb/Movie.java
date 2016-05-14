package com.example.jbt.omdb;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;


public class Movie implements Parcelable {

    private final static long NOT_IN_DB = -1;

    private long mId;
    private String mSubject;
    private String mBody;
    private String mUrl;
    private String mImdbId;
    private Bitmap mImage;


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
        mImage = image;
    }

    public Movie(String subject) {
        this(subject, "", "", "", null);
    }

    protected Movie(Parcel in) {

        mId = in.readLong();
        mSubject = in.readString();
        mBody = in.readString();
        mUrl = in.readString();
        mImdbId = in.readString();
        mImage = in.readParcelable(Bitmap.class.getClassLoader());
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
        return mImage;
    }

    public void setImage(Bitmap image) { mImage = image; }

    public boolean isSavedInDB() { return mId != NOT_IN_DB; }

    @Override
    public String toString() {
        return mSubject;
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
//        dest.writeParcelable(mImage, flags);
    }
}
