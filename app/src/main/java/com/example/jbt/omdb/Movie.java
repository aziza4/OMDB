package com.example.jbt.omdb;

import java.io.Serializable;


public class Movie implements Serializable {

    private int mId;
    private String mSubject;
    private String mBody;
    private String mUrl;
    private String mImdbId;

    public Movie(int _id, String subject, String body, String url, String imdbId) {
        this(subject, body, url, imdbId);
        mId = _id;
    }

    public Movie(String subject, String body, String url, String imdbId) {
        mSubject = subject;
        mBody = body;
        mUrl = url;
        mImdbId = imdbId;
    }

    public Movie(String subject) {
        mSubject = subject;
    }

    public int getId() {
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
