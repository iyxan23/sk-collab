package com.iyxan23.sketch.collab.models;

import androidx.annotation.Nullable;

import com.iyxan23.sketch.collab.Util;

import org.json.JSONException;

import java.util.LinkedList;

import name.fraser.neil.plaintext.diff_match_patch;

public class SketchwareProjectChanges {
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
                        Util.decrypt(before),
                        Util.decrypt(after)
                )
        );
    }
}
