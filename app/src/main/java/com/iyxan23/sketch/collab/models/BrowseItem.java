package com.iyxan23.sketch.collab.models;

import android.os.Parcel;
import android.os.Parcelable;

public class BrowseItem implements Parcelable {
    public int project_id;
    public String username;
    public String project_name;
    public String uid;
    public int latest_commit_timestamp;

    protected BrowseItem(Parcel in) {
        project_id = in.readInt();
        username = in.readString();
        project_name = in.readString();
        uid = in.readString();
        latest_commit_timestamp = in.readInt();
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
        dest.writeInt(project_id);
        dest.writeString(username);
        dest.writeString(project_name);
        dest.writeString(uid);
        dest.writeInt(latest_commit_timestamp);
    }
}
