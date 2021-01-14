package com.iyxan23.sketch.collab.models;

import android.os.Environment;

import com.iyxan23.sketch.collab.Util;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class SketchwareProject {

    // These variables should be in their raw encrypted format
    public byte[] logic;
    public byte[] view;
    public byte[] resource;
    public byte[] library;
    public byte[] file;
    public byte[] mysc_project;

    private int project_id = -1;

    public SketchwareProject(byte[] logic, byte[] view, byte[] resource, byte[] library, byte[] file, byte[] mysc_project) {
        this.logic = logic;
        this.view = view;
        this.resource = resource;
        this.library = library;
        this.file = file;
        this.mysc_project = mysc_project;

        try { setProjectID(); } catch (JSONException ignored) { }
    }

    public String sha512sum() throws JSONException {
        // This is kinda a "dangerous" way of doing it, if the project is super big, it can cause
        // OutOfMemory Exception, but i'll leave it here for now

        // Ik this looks a very dumb, but i currently don't have any other way to do it sooo
        byte[] joined =
                Util.joinByteArrays(file,
                        Util.joinByteArrays(logic,
                                Util.joinByteArrays(library,
                                        Util.joinByteArrays(view,
                                                Util.joinByteArrays(resource,
                                                        mysc_project)))));

        // Join the resources too
        // Check if the project id is set
        if (project_id == -1) setProjectID();

        File images_folder = new File(Environment.getExternalStorageDirectory() + "/.sketchware/resources/" + project_id + "/images/");
        File sounds_folder = new File(Environment.getExternalStorageDirectory() + "/.sketchware/resources/" + project_id + "/sounds/");
        File icons_folder = new File(Environment.getExternalStorageDirectory() + "/.sketchware/resources/" + project_id + "/icons/");
        File fonts_folder = new File(Environment.getExternalStorageDirectory() + "/.sketchware/resources/" + project_id + "/fonts/");

        // Get the images
        for (File file: images_folder.listFiles()) {
            try {
                joined = Util.joinByteArrays(joined, Util.readFile(new FileInputStream(file)));
            } catch (FileNotFoundException ignored) { }
        }

        // Get the sounds
        for (File file: sounds_folder.listFiles()) {
            try {
                joined = Util.joinByteArrays(joined, Util.readFile(new FileInputStream(file)));
            } catch (FileNotFoundException ignored) { }
        }

        // Get the icons
        for (File file: icons_folder.listFiles()) {
            try {
                joined = Util.joinByteArrays(joined, Util.readFile(new FileInputStream(file)));
            } catch (FileNotFoundException ignored) { }
        }

        // Get the fonts
        for (File file: fonts_folder.listFiles()) {
            try {
                joined = Util.joinByteArrays(joined, Util.readFile(new FileInputStream(file)));
            } catch (FileNotFoundException ignored) { }
        }

        // Return the shasum
        return Util.sha512(joined);
    }

    public int getProjectID() throws JSONException {
        if (project_id != -1)
            return project_id;
        else
            setProjectID();

        return getProjectID();
    }

    private void setProjectID() throws JSONException {
        JSONObject mysc_project_ = new JSONObject(Util.decrypt(mysc_project));
        project_id = Integer.parseInt(mysc_project_.getString("sc_id"));
    }
}
