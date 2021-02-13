package com.iyxan23.sketch.collab.pman;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.iyxan23.sketch.collab.R;
import com.iyxan23.sketch.collab.models.SketchwareProject;

import org.json.JSONException;

public class ViewProjectActivity extends AppCompatActivity {

    SketchwareProject project;
    boolean is_sketchcollab_project;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_project);

        Intent intent = getIntent();

        // Get the project extra
        project = intent.getParcelableExtra("project");

        if (project.metadata == null) {
            finish_corrupted();
        }

        // Get if this project is a sketchcollab project
        try {
            is_sketchcollab_project = project.isSketchCollabProject();
        } catch (JSONException e) {
            finish_corrupted();
        }

        if (is_sketchcollab_project) {
            // TODO
        }
    }

    private void finish_corrupted() {
        Toast.makeText(this, "This project is corrupted", Toast.LENGTH_SHORT).show();

        finish();
    }

    public void openOnlineProject(View view) {
    }

    public void back(View view) {
        onBackPressed();
    }
}