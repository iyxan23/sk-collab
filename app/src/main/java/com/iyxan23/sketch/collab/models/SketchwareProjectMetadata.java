package com.iyxan23.sketch.collab.models;

import org.json.JSONException;
import org.json.JSONObject;

public class SketchwareProjectMetadata {

    public String project_name;
    public String project_package;
    public String app_name;
    public int id = -1;

    public SketchwareProjectMetadata(JSONObject mysc_project) {
        try {
            project_name = mysc_project.getString("my_ws_name");
            project_package = mysc_project.getString("my_sc_pkg_name");
            app_name = mysc_project.getString("my_app_name");
            id = Integer.parseInt(mysc_project.getString("sc_id"));
        } catch (JSONException ignored) { }
    }
}
