package com.iyxan23.sketch.collab.models;

import android.content.Intent;

public class SearchItem {
    public Intent intent;

    public String title;
    public String subtitle;

    public SearchItem() {}

    public SearchItem(String title) {
        this.title = title;
    }

    public SearchItem(String title, String subtitle) {
        this.title = title;
        this.subtitle = subtitle;
    }

    public SearchItem(Intent intent, String title, String subtitle) {
        this.intent = intent;
        this.title = title;
        this.subtitle = subtitle;
    }
}
