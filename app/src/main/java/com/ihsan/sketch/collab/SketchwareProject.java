package com.ihsan.sketch.collab;

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

    @Override
    public String toString() {
        return "SketchwareProject{" +
                "package_='" + package_ + '\'' +
                ", version='" + version + '\'' +
                ", name='" + name + '\'' +
                ", coname='" + coname + '\'' +
                '}';
    }
}
