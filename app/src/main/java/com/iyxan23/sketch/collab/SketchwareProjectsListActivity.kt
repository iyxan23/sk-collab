package com.iyxan23.sketch.collab

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.RecyclerView
import com.iyxan23.sketch.collab.adapters.SketchwareProjectRecyclewViewAdapter
import com.iyxan23.sketch.collab.models.SketchwareProject

class SketchwareProjectsListActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sketchware_projects_list)

        val sketchwareProjectsListRecyclerView: RecyclerView = findViewById(R.id.sketchware_project_list)

        // Get all sketchware projects
        val sketchwareProjects: ArrayList<SketchwareProject> = Util.getSketchwareProjects()

        // Pass it onto the recyclerview
        val adapter = SketchwareProjectRecyclewViewAdapter(sketchwareProjects, this)
        sketchwareProjectsListRecyclerView.adapter = adapter

        adapter.notifyDataSetChanged()
    }
}