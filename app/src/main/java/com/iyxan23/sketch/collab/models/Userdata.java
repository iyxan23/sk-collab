package com.iyxan23.sketch.collab.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Objects;

public class Userdata implements Parcelable {
    private String name;
    private String uid;

    public Userdata(String name, String uid) {
        this.name = name;
        this.uid = uid;
    }

    protected Userdata(Parcel in) {
        name = in.readString();
        uid = in.readString();
    }

    public static final Creator<Userdata> CREATOR = new Creator<Userdata>() {
        @Override
        public Userdata createFromParcel(Parcel in) {
            return new Userdata(in);
        }

        @Override
        public Userdata[] newArray(int size) {
            return new Userdata[size];
        }
    };

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(uid);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Userdata userdata = (Userdata) o;

        return Objects.equals(name, userdata.name) &&
                Objects.equals(uid, userdata.uid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, uid);
    }

    @Override
    public String toString() {
        return "Userdata{" +
                "name='" + name + '\'' +
                ", uid='" + uid + '\'' +
                '}';
    }
}
