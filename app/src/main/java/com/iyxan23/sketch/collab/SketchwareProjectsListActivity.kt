package com.iyxan23.sketch.collab

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.iyxan23.sketch.collab.adapters.SketchwareProjectRecyclewViewAdapter
import com.iyxan23.sketch.collab.models.SketchwareProject

class SketchwareProjectsListActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        // Get rid of the flasing white color while doing shared element transition
        window.enterTransition = null
        window.exitTransition = null

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sketchware_projects_list)

        val sketchwareProjectsListRecyclerView: RecyclerView = findViewById(R.id.sketchware_project_list)

        // Get all sketchware projects
        val sketchwareProjects: ArrayList<SketchwareProject> = Util.getSketchwareProjects()

        if (sketchwareProjects.size == 0) {
            // There is no sketchware projects (or failed to get sketchware projects)
            Toast.makeText(this, "There is no sketchware projects detected, Or failed to get sketchware projects", Toast.LENGTH_LONG).show()
        }

        // Pass it onto the recyclerview
        val adapter = SketchwareProjectRecyclewViewAdapter(sketchwareProjects, this)
        sketchwareProjectsListRecyclerView.adapter = adapter
        sketchwareProjectsListRecyclerView.layoutManager = LinearLayoutManager(this)

        adapter.notifyDataSetChanged()
    }
}