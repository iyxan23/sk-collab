package com.ihsan.sketch.collab;

import androidx.annotation.NonNull;

public class SketchwareProject {
    String package_;
    String version;
    String name;
    String coname;
    String id;

    public SketchwareProject(String name, String version, String package_, String coname, String id) {
        this.package_ = package_;
        this.version = version;
        this.name = name;
        this.coname = coname;
        this.id = id;
    }

    @NonNull
    @Override
    public String toString() {
        return name + " - " + package_;
    }
}
