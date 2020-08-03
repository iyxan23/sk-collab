package com.ihsan.sketch.collab;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Toast;

import java.io.File;

public class ViewProjectActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_project);
        getSupportActionBar().setElevation(0f);

        Intent i = getIntent();
        String id = i.getStringExtra("id");

        SketchwareProject current = null;
        for (SketchwareProject project: Util.localProjects) {
            if (project.id.equals(id)) {
                current = project;
            }
        }

        if (current == null) {
            Toast.makeText(this, "ERROR: PROJECT NOT FOUND", Toast.LENGTH_LONG).show();
            finish();
        }

        final File project = new File(Environment.getExternalStorageDirectory()
                .getAbsolutePath() + "/.sketchware/mysc/list/" + id + "/project");
        final File view = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/.sketchware/data/" + id + "/view");
        final File logic = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/.sketchware/data/" + id + "/logic");
        final File resource = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/.sketchware/data/" + id + "/resource");
        final File library = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/.sketchware/data/" + id + "/library");
    }
}