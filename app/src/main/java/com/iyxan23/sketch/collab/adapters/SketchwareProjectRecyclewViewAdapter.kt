package com.iyxan23.sketch.collab.adapters

import android.content.Context
import java.util.ArrayList
import java.util.HashMap
import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup
import android.view.LayoutInflater
import com.iyxan23.sketch.collab.R
import android.os.Build
import android.view.View
import android.widget.TextView
import android.widget.ProgressBar
import android.widget.Button
import android.widget.Toast
import com.iyxan23.sketch.collab.models.SketchwareProject

class SketchwareProjectRecyclewViewAdapter(var data: ArrayList<SketchwareProject>, var mContext: Context) : RecyclerView.Adapter<SketchwareProjectRecyclewViewAdapter.ViewHolder>() {
    fun updateData(data: ArrayList<SketchwareProject>) {
        this.data = data
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.rv_sketchware_project, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentData: SketchwareProject = data[position]
        holder.project_name.text = currentData.metadata.projectName
        holder.project_details.text = currentData.metadata.packageName + " (" + currentData.metadata.id + ")"

        holder.details.text = "Sketchware Project"

        holder.uploadButton.setOnClickListener {
            Toast.makeText(holder.itemView.context, "Upload clicked", Toast.LENGTH_SHORT).show()
            // TODO("Redirect user to CommitActivity")
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var project_name: TextView = itemView.findViewById(R.id.project_name)
        var project_details: TextView = itemView.findViewById(R.id.project_details)
        var details: TextView = itemView.findViewById(R.id.details)
        var uploadButton: Button = itemView.findViewById(R.id.upload_button)

    }
}