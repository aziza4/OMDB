package com.example.jbt.omdb;

import java.io.Serializable;


public class Movie implements Serializable {
    private int _id;
    private String subject;
    private String body;
    private String url;
    private String imdbId;

    public Movie(int _id, String subject, String body, String url, String imdbId) {
        this(subject, body, url, imdbId);
        this._id = _id;
    }

    public Movie(String subject, String body, String url, String imdbId) {
        this.subject = subject;
        this.body = body;
        this.url = url;
        this.imdbId = imdbId;
    }

    public Movie(String subject) {
        this.subject = subject;
    }

    public int get_id() {
        return _id;
    }

    public String getSubject() {
        return subject;
    }

    public String getBody() {
        return body;
    }

    public String getUrl() {
        return url;
    }

    public String getImdbId() {
        return imdbId;
    }

    @Override
    public String toString() {
        return subject;
    }
}
