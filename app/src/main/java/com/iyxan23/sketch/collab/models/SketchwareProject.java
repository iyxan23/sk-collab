package com.iyxan23.sketch.collab.models;

import android.os.Environment;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;

import com.iyxan23.sketch.collab.Util;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class SketchwareProject implements Parcelable {

    // These variables should be in their raw encrypted format
    public byte[] logic;
    public byte[] view;
    public byte[] resource;
    public byte[] library;
    public byte[] file;
    public byte[] mysc_project;

    public SketchwareProjectMetadata metadata;

    private int project_id = -1;

    public SketchwareProject() {}

    public SketchwareProject(byte[] logic, byte[] view, byte[] resource, byte[] library, byte[] file, byte[] mysc_project) {
        this.logic = logic;
        this.view = view;
        this.resource = resource;
        this.library = library;
        this.file = file;
        this.mysc_project = mysc_project;

        try {
            setProjectID();
            metadata = new SketchwareProjectMetadata(new JSONObject(Util.decrypt(mysc_project)));

        } catch (JSONException ignored) { }
    }

    public void applyChanges() throws IOException {
        String project_folder = Environment.getExternalStorageDirectory().getAbsolutePath() + "/.sketchware/data/" + project_id + "/";

        File file       = new File(project_folder + "file");
        File logic      = new File(project_folder + "logic");
        File library    = new File(project_folder + "library");
        File view       = new File(project_folder + "view");
        File resource   = new File(project_folder + "resource");

        File mysc_project = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/.sketchware/mysc/list/" + project_id + "/project");

        Util.writeFile(file     , this.file     );
        Util.writeFile(logic    , this.logic    );
        Util.writeFile(library  , this.library  );
        Util.writeFile(view     , this.view     );
        Util.writeFile(resource , this.resource );

        Util.writeFile(mysc_project, this.mysc_project);
    }

    @Nullable
    public JSONObject getMyscProject() {
        try {
            return new JSONObject(Util.decrypt(mysc_project));
        } catch (JSONException e) {
            return null;
        }
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
                                                resource))));

        // Join the resources too
        // Check if the project id is set
        if (project_id == -1) setProjectID();

        /*
        File images_folder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/.sketchware/resources/" + project_id + "/images/");
        File sounds_folder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/.sketchware/resources/" + project_id + "/sounds/");
        File icons_folder  = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/.sketchware/resources/" + project_id + "/icons/" );
        File fonts_folder  = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/.sketchware/resources/" + project_id + "/fonts/" );

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
         */

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

    public boolean isSketchCollabProject() throws JSONException {
        return new JSONObject(Util.decrypt(mysc_project)).has("sk-collab-key");
    }

    public String getSketchCollabKey() throws JSONException {
        return new JSONObject(Util.decrypt(mysc_project)).getString("sk-collab-key");
    }

    public String getSketchCollabAuthorUid() throws JSONException {
        return new JSONObject(Util.decrypt(mysc_project)).getString("sk-collab-owner");
    }

    public boolean isSketchCollabProjectPublic() throws JSONException {
        return new JSONObject(Util.decrypt(mysc_project)).getString("sk-collab-project-visibility").equals("public");
    }

    public String getSketchCollabLatestCommitID() throws JSONException {
        return new JSONObject(Util.decrypt(mysc_project)).getString("sk-collab-latest-commit");
    }

    protected SketchwareProject(Parcel in) {
        logic = in.createByteArray();
        view = in.createByteArray();
        resource = in.createByteArray();
        library = in.createByteArray();
        file = in.createByteArray();
        mysc_project = in.createByteArray();
        project_id = in.readInt();

        try {
            setProjectID();
            metadata = new SketchwareProjectMetadata(new JSONObject(Util.decrypt(mysc_project)));
        } catch (JSONException ignored) { }
    }

    @NotNull
    public String toString() {
        try {
            return "SketchwareProject with ID " + getProjectID() + "\nisSketchCollabProject " + isSketchCollabProject() + "\nsha512sum: " + sha512sum();
        } catch (JSONException e) {
            e.printStackTrace();
            return "SketchwareProject";
        }
    }


    public static final Creator<SketchwareProject> CREATOR = new Creator<SketchwareProject>() {
        @Override
        public SketchwareProject createFromParcel(Parcel in) {
            return new SketchwareProject(in);
        }

        @Override
        public SketchwareProject[] newArray(int size) {
            return new SketchwareProject[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByteArray(logic);
        dest.writeByteArray(view);
        dest.writeByteArray(resource);
        dest.writeByteArray(library);
        dest.writeByteArray(file);
        dest.writeByteArray(mysc_project);
        dest.writeInt(project_id);
    }
}
