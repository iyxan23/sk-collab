package com.ihsan.sketch.collab;

import android.util.Base64;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

public class SketchwareProjectData {

    // Data must be raw and encrypted with base64 to avoid binary problems
    private String file;
    private String view;
    private String resource;
    private String logic;
    private String library;
    private SketchwareProject data;

    public SketchwareProjectData(String file, String view, String resource, String logic, String library, SketchwareProject data) {
        this.file = Util.base64encrypt(file);
        this.view = Util.base64encrypt(view);
        this.resource = Util.base64encrypt(resource);
        this.logic = Util.base64encrypt(logic);
        this.library = Util.base64encrypt(library);
        this.data = data;
    }

    public String getEncryptedFile() {
        return file;
    }

    public String getEncryptedView_() {
        return view;
    }

    public String getEncryptedResource() {
        return resource;
    }

    public String getEncryptedLogic() {
        return logic;
    }

    public String getEncryptedLibrary() {
        return library;
    }

    public SketchwareProject getData() {
        return data;
    }

    public void setFile(String file) {
        this.file = Util.base64encrypt(file);
    }

    public void setView(String view) {
        this.view = Util.base64encrypt(view);
    }

    public void setResource(String resource) {
        this.resource = Util.base64encrypt(resource);
    }

    public void setLogic(String logic) {
        this.logic = Util.base64encrypt(logic);
    }

    public void setLibrary(String library) {
        this.library = Util.base64encrypt(library);
    }

    public void setData(SketchwareProject data) {
        this.data = data;
    }

    public JSONObject getJSON() throws JSONException {
        JSONObject result = new JSONObject();
        String file_decrypted = Util.decrypt(Util.base64decrypt(file));
        String view_decrypted = Util.decrypt(Util.base64decrypt(view));
        String logic_decrypted = Util.decrypt(Util.base64decrypt(logic));
        String library_decrypted = Util.decrypt(Util.base64decrypt(library));
        String resource_decrypted = Util.decrypt(Util.base64decrypt(resource));

        return result.put("file", file_decrypted)
                     .put("view", view_decrypted)
                     .put("logic", logic_decrypted)
                     .put("library", library_decrypted)
                     .put("resource", resource_decrypted);
    }
}
