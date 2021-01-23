package com.iyxan23.sketch.collab.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.Timestamp;

public class BrowseItem implements Parcelable {
    public String project_id;
    public String username;
    public String project_name;
    public String uid;
    public Timestamp latest_commit_timestamp;

    public BrowseItem(String project_id, String username, String project_name, String uid, Timestamp latest_commit_timestamp) {
        this.project_id = project_id;
        this.username = username;
        this.project_name = project_name;
        this.uid = uid;
        this.latest_commit_timestamp = latest_commit_timestamp;
    }

    protected BrowseItem(Parcel in) {
        project_id = in.readString();
        username = in.readString();
        project_name = in.readString();
        uid = in.readString();
        latest_commit_timestamp = in.readParcelable(Timestamp.class.getClassLoader());
    }

    public static final Creator<BrowseItem> CREATOR = new Creator<BrowseItem>() {
        @Override
        public BrowseItem createFromParcel(Parcel in) {
            return new BrowseItem(in);
        }

        @Override
        public BrowseItem[] newArray(int size) {
            return new BrowseItem[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(project_id);
        dest.writeString(username);
        dest.writeString(project_name);
        dest.writeString(uid);
        dest.writeParcelable(latest_commit_timestamp, flags);
    }
}
