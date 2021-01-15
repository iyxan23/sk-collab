package com.iyxan23.sketch.collab;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.iyxan23.sketch.collab.adapters.SketchwareProjectAdapter;

public class SketchwareProjectsListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sketchware_projects_list);

        // Get the recyclerview
        RecyclerView projecs_list = findViewById(R.id.sketchware_project_list);

        // Get the adapter
        SketchwareProjectAdapter adapter = new SketchwareProjectAdapter(Util.fetch_sketchware_projects(), this);

        // Set the adapter
        projecs_list.setAdapter(adapter);
    }
}
