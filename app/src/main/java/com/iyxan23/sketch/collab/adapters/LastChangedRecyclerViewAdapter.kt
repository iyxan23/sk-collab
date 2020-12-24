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

class LastChangedRecyclerViewAdapter(var data: ArrayList<HashMap<String, Any>>, var mContext: Context) : RecyclerView.Adapter<LastChangedRecyclerViewAdapter.ViewHolder>() {
    fun updateData(data: ArrayList<HashMap<String, Any>>) {
        this.data = data
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.rv_last_changes, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentData = data[position]
        holder.project_name.text = currentData["project_name"] as String?
        holder.project_details.text = currentData["project_package"] as String? + " (" + currentData["project_id"] as String? + ")"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            holder.addition_logic.setProgress((currentData["logic_added"] as Int?)!!, true)
            holder.deletion_logic.setProgress((currentData["logic_deleted"] as Int?)!!, true)
            holder.addition_view.setProgress((currentData["view_added"] as Int?)!!, true)
            holder.deletion_view.setProgress((currentData["view_deleted"] as Int?)!!, true)

        } else {
            // For API 23 and lower
            holder.addition_logic.progress = (currentData["logic_added"] as Int?)!!
            holder.deletion_logic.progress = (currentData["logic_deleted"] as Int?)!!
            holder.addition_view.progress = (currentData["view_added"] as Int?)!!
            holder.deletion_view.progress = (currentData["view_deleted"] as Int?)!!
        }

        holder.chg_summary_logic.text = currentData["sm_logic"] as String?
        holder.chg_summary_view.text = currentData["sm_view"] as String?
        holder.details.text = currentData["details"] as String?

        holder.push_button.setOnClickListener {
            TODO("Redirect user to CommitActivity")
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var main_content: View
        var project_name: TextView
        var project_details: TextView
        var addition_logic: ProgressBar
        var deletion_logic: ProgressBar
        var addition_view: ProgressBar
        var deletion_view: ProgressBar
        var chg_summary_logic: TextView
        var chg_summary_view: TextView
        var details: TextView
        var push_button: Button

        init {
            main_content = itemView.findViewById(R.id.constraintLayout)
            project_name = itemView.findViewById(R.id.project_name)
            project_details = itemView.findViewById(R.id.project_details)
            addition_logic = itemView.findViewById(R.id.addition_logic)
            deletion_logic = itemView.findViewById(R.id.deletion_logic)
            addition_view = itemView.findViewById(R.id.addition_view)
            deletion_view = itemView.findViewById(R.id.deletion_view)
            chg_summary_logic = itemView.findViewById(R.id.change_summary_logic)
            chg_summary_view = itemView.findViewById(R.id.change_summary_view)
            details = itemView.findViewById(R.id.details)
            push_button = itemView.findViewById(R.id.upload_button)
        }
    }
}