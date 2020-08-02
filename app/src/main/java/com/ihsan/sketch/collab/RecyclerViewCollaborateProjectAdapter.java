package com.ihsan.sketch.collab;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class RecyclerViewCollaborateProjectAdapter extends RecyclerView.Adapter<RecyclerViewCollaborateProjectAdapter.ViewHolder> {

    private ArrayList<OnlineProject> projects;

    public RecyclerViewCollaborateProjectAdapter(ArrayList<OnlineProject> collaborateProjects) {
        this.projects = collaborateProjects;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_item_collaborate_layout, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OnlineProject onlineProject = projects.get(position);
        holder.title.setText(onlineProject.getTitle());
        holder.author.setText("Started by " + onlineProject.getUser_author_name());
        if (onlineProject.commits.size() != 0) {
            holder.last_commit.setText(onlineProject.commits.get(onlineProject.commits.size() - 1).author_name);
            holder.last_commit_time.setText(onlineProject.commits.get(onlineProject.commits.size() - 1).timestamp);
        } else {
            holder.latest_commit.setText("There is no commits");
        }
    }

    @Override
    public int getItemCount() {
        return projects.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView title;
        TextView author;
        TextView last_commit;
        TextView last_commit_time;
        TextView latest_commit;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title_col);
            author = itemView.findViewById(R.id.author_col);
            last_commit = itemView.findViewById(R.id.user_last_commit_col);
            last_commit_time = itemView.findViewById(R.id.time_last_commit_col);
            latest_commit = itemView.findViewById(R.id.latest_commit_col);
        }
    }
}
