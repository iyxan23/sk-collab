package com.ihsan.sketch.collab;

public class Commit {
    String uid;
    String author_name;
    int timestamp;
    String changes;

    public Commit(String uid, String author_name, int timestamp, String changes) {
        this.uid = uid;
        this.author_name = author_name;
        this.timestamp = timestamp;
        this.changes = changes;
    }
}
