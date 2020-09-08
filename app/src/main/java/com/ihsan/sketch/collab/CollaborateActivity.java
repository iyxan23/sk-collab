package com.ihsan.sketch.collab;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.ArrayList;

import static com.ihsan.sketch.collab.Util.decrypt;
import static com.ihsan.sketch.collab.Util.listDir;

public class CollaborateActivity extends AppCompatActivity {

    private ArrayList<SketchwareProject> sketchwareProjects = new ArrayList<>();
    private static final String TAG = "CollaborateActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collaborate);

        Intent intent = getIntent();

        String key = intent.getStringExtra("key");
        String id = intent.getStringExtra("id");

        storagePerms();

        boolean exist = false;
        for (SketchwareProject project: sketchwareProjects) {
            if (project.id == id) {
                exist = true;
                break;
            }
        }

        if (!exist) {
            Toast.makeText(this, "ERROR CA47 : NO SKETCHWARE PROJECT FOUND (" + id + ")", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void storagePerms() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission("android.permission.READ_EXTERNAL_STORAGE") == PackageManager.PERMISSION_DENIED ||
                    checkSelfPermission("android.permission.WRITE_EXTERNAL_STORAGE") == PackageManager.PERMISSION_DENIED) {
                requestPermissions(new String[]{"android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE"}, 1000);
            } else {
                sketchwareProjects = Util.getSketchwareProjects();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1000) {
            sketchwareProjects = Util.getSketchwareProjects();
        }
    }
}