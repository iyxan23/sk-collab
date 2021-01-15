package com.iyxan23.sketch.collab;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.iyxan23.sketch.collab.adapters.SketchwareProjectAdapter;
import com.iyxan23.sketch.collab.models.SketchwareProject;

import java.util.ArrayList;

public class SketchwareProjectsListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sketchware_projects_list);

        // Get the recyclerview
        RecyclerView projecs_list = findViewById(R.id.sketchware_project_list);

        // Get and set the adapter
        SketchwareProjectAdapter adapter = new SketchwareProjectAdapter(this);
        projecs_list.setAdapter(adapter);

        // Load projects in a different thread
        new Thread(() -> {
            // Fetch projects
            ArrayList<SketchwareProject> projects = Util.fetch_sketchware_projects();

            // Hide the progressbar
            findViewById(R.id.progressBar_swplist).setVisibility(View.GONE);

            // Update the data
            adapter.updateView(projects);
        }).start();
    }
}
