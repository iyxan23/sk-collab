package com.iyxan23.sketch.collab.models;

import android.content.Intent;
import android.text.SpannableString;

public class SearchItem {
    public Intent intent;

    public SpannableString title;
    public String subtitle;

    public SearchItem() {}

    public SearchItem(String title) {
        this.title = new SpannableString(title);
    }

    public SearchItem(String title, String subtitle) {
        this.title = new SpannableString(title);
        this.subtitle = subtitle;
    }

    public SearchItem(SpannableString title) {
        this.title = title;
    }

    public SearchItem(SpannableString title, String subtitle) {
        this.title = title;
        this.subtitle = subtitle;
    }

    public SearchItem(Intent intent, SpannableString title, String subtitle) {
        this.intent = intent;
        this.title = title;
        this.subtitle = subtitle;
    }
}
