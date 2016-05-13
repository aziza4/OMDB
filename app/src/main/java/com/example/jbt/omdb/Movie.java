package com.example.jbt.omdb;

import java.io.Serializable;


public class Movie implements Serializable {

    private final static int NOT_IN_DB = -1;

    private long mId;
    private String mSubject;
    private String mBody;
    private String mUrl;
    private String mImdbId;

    public Movie(long _id, String subject, String body, String url, String imdbId) {
        this(subject, body, url, imdbId);
        mId = _id;
    }

    public Movie(String subject, String body, String url, String imdbId) {
        mId = NOT_IN_DB;
        mSubject = subject;
        mBody = body;
        mUrl = url;
        mImdbId = imdbId;
    }

    public Movie(String subject) {
        this(subject, "", "", "");
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

    @Override
    public String toString() {
        return mSubject;
    }
}
