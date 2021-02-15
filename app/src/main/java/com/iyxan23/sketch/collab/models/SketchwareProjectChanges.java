package com.iyxan23.sketch.collab.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.iyxan23.sketch.collab.Util;
import com.iyxan23.sketch.collab.helpers.PatchHelper;

import org.json.JSONException;

import java.util.HashMap;
import java.util.Map;

import name.fraser.neil.plaintext.diff_match_patch;

public class SketchwareProjectChanges implements Parcelable {
    public SketchwareProject before;
    public SketchwareProject after;

    // Masks
    public static final int LOGIC               = 0b000001;
    public static final int VIEW                = 0b000010;
    public static final int LIBRARY             = 0b000100;
    public static final int FILE                = 0b001000;
    public static final int RESOURCES           = 0b010000;
    public static final int RESOURCES_FOLDER    = 0b100000;

    public SketchwareProjectChanges(SketchwareProject before, SketchwareProject after) {
        this.before = before;
        this.after = after;
    }

    protected SketchwareProjectChanges(Parcel in) {
        before = in.readParcelable(SketchwareProject.class.getClassLoader());
        after = in.readParcelable(SketchwareProject.class.getClassLoader());
    }

    public static final Creator<SketchwareProjectChanges> CREATOR = new Creator<SketchwareProjectChanges>() {
        @Override
        public SketchwareProjectChanges createFromParcel(Parcel in) {
            return new SketchwareProjectChanges(in);
        }

        @Override
        public SketchwareProjectChanges[] newArray(int size) {
            return new SketchwareProjectChanges[size];
        }
    };

    public boolean isEqual() throws JSONException {
        return before.sha512sum().equals(after.sha512sum());
    }

    public int getFilesChanged() {
        int output = 0;

        if (!Util.sha512(before.file)       .equals(Util.sha512(after.file      ))) output |= FILE      ;
        if (!Util.sha512(before.view)       .equals(Util.sha512(after.view      ))) output |= VIEW      ;
        if (!Util.sha512(before.logic)      .equals(Util.sha512(after.logic     ))) output |= LOGIC     ;
        if (!Util.sha512(before.library)    .equals(Util.sha512(after.library   ))) output |= LIBRARY   ;
        if (!Util.sha512(before.resource)   .equals(Util.sha512(after.resource  ))) output |= RESOURCES ;
        // TODO: THIS
        /* if (!Util.sha512(before.file).equals(Util.sha512(after.file))) output |= RESOURCES_FOLDER; */
        return output;
    }

    public String getPatch(int type) {
        diff_match_patch dmp = new diff_match_patch();

        // Unlimited timeout
        dmp.Diff_Timeout = 0f;

        byte[] before;
        byte[] after;

        if (type == LOGIC) {
            before = this.before.logic;
            after = this.after.logic;

        } else if (type == VIEW) {
            before = this.before.view;
            after = this.after.view;

        } else if (type == FILE) {
            before = this.before.file;
            after = this.after.file;

        } else if (type == LIBRARY) {
            before = this.before.library;
            after = this.after.library;

        } else if (type == RESOURCES) {
            before = this.before.resource;
            after = this.after.resource;

        } else {
            // ..bruh
            return null;
        }

        return dmp.patch_toText(
                dmp.patch_make(
                        Util.decrypt(after),
                        Util.decrypt(before)
                )
        );
    }

    public HashMap<String, String> generatePatch() {
        HashMap<String, String> patch = new HashMap<>();

        // Get the changed files
        int files_changed = getFilesChanged();

        // Pack every patches into one map
        int[] data_keys = new int[] {
                SketchwareProjectChanges.LOGIC      ,
                SketchwareProjectChanges.VIEW       ,
                SketchwareProjectChanges.FILE       ,
                SketchwareProjectChanges.LIBRARY    ,
                SketchwareProjectChanges.RESOURCES  ,
        };

        String[] data_keys_str = new String[] {
                "logic"      ,
                "view"       ,
                "file"       ,
                "library"    ,
                "resources"  ,
        };

        for (int index = 0; index < data_keys.length; index++) {
            if ((files_changed & data_keys[index]) == data_keys[index]) {
                patch.put(data_keys_str[index], getPatch(data_keys[index]));
            }
        }

        return patch;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(before, flags);
        dest.writeParcelable(after, flags);
    }

    @Override
    public String toString() {
        return PatchHelper.convert_to_readable_patch(this);
    }
}
