package com.iyxan23.sketch.collab.models;

import android.os.Environment;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;

import com.iyxan23.sketch.collab.Util;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
        String project_folder = Environment.getExternalStorageDirectory().getAbsolutePath() + "/.sketchware/data/" + project_id;
        FileOutputStream file = new FileOutputStream(new File(project_folder + "/file"));
        FileOutputStream logic = new FileOutputStream(new File(project_folder + "/logic"));
        FileOutputStream library = new FileOutputStream(new File(project_folder + "/library"));
        FileOutputStream view = new FileOutputStream(new File(project_folder + "/view"));
        FileOutputStream resource = new FileOutputStream(new File(project_folder + "/resource"));

        FileOutputStream mysc_project = new FileOutputStream(new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/.sketchware/mysc/list/" + project_id + "/project"));

        file.write(this.file);
        file.flush();
        file.close();

        logic.write(this.logic);
        file.flush();
        logic.close();

        library.write(this.library);
        library.flush();
        library.close();

        view.write(this.view);
        view.flush();
        view.close();

        resource.write(this.resource);
        resource.flush();
        resource.close();

        mysc_project.write(this.mysc_project);
        mysc_project.flush();
        mysc_project.close();
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

        // Delete some keys on mysc/project
        // HELP: I'm kinda scared of this, will java pass it's reference or it's value?
        byte[] mysc_project_mod = mysc_project;
        JSONObject obj = new JSONObject(Util.decrypt(mysc_project_mod));

        obj.optInt("sc_id");

        if (isSketchCollabProject()) {
            obj.optString("sk-collab-key");
            obj.optString("sk-collab-author");
        }

        mysc_project_mod = Util.encrypt(obj.toString().getBytes());

        // Ik this looks a very dumb, but i currently don't have any other way to do it sooo
        byte[] joined =
                Util.joinByteArrays(file,
                        Util.joinByteArrays(logic,
                                Util.joinByteArrays(library,
                                        Util.joinByteArrays(view,
                                                Util.joinByteArrays(resource,
                                                        mysc_project_mod)))));

        // Join the resources too
        // Check if the project id is set
        if (project_id == -1) setProjectID();

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
        return new JSONObject(Util.decrypt(mysc_project)).getString("sk-collab-author");
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
