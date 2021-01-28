package com.iyxan23.sketch.collab.models;

import com.google.firebase.Timestamp;

import java.util.Map;

public class Commit {
    public String name;
    public String author;
    public String author_username;
    public String id;
    public String sha512sum;
    public Timestamp timestamp;
    public Map<String, String> patch;

    public Commit() {}

    public Commit(String name, String author, String author_username, String id, String sha512sum, Timestamp timestamp, Map<String, String> patch) {
        this.name = name;
        this.author = author;
        this.author_username = author_username;
        this.id = id;
        this.sha512sum = sha512sum;
        this.timestamp = timestamp;
        this.patch = patch;
    }

    public Commit(String name, String author, String sha512sum, Timestamp timestamp, Map<String, String> patch) {
        this.name = name;
        this.author = author;
        this.sha512sum = sha512sum;
        this.timestamp = timestamp;
        this.patch = patch;
    }
}
