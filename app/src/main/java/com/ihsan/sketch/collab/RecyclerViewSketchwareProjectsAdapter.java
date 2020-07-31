package com.ihsan.sketch.collab;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityOptionsCompat;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;


public class RecyclerViewSketchwareProjectsAdapter extends RecyclerView.Adapter<RecyclerViewSketchwareProjectsAdapter.ViewHolder> {
    private static final String TAG = "OnlineProjsAdapter";

    private ArrayList<SketchwareProject> datas;
    private Activity activity;

    public RecyclerViewSketchwareProjectsAdapter(ArrayList<SketchwareProject> datas, Activity activity) {
        this.datas = datas;
        this.activity = activity;
    }

    public void updateView(ArrayList<SketchwareProject> datas) {
        this.datas = datas;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.sketchware_proj_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);

        return viewHolder;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        Log.d(TAG, "onBindViewHolder: called.");
        SketchwareProject project = datas.get(position);
        holder.title.setText(project.name);
        holder.subtitle.setText(project.coname.concat(" • ").concat(project.package_));
        holder.version.setText(project.version);

        holder.upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(activity, UploadActivity.class);
                ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(activity, holder.upload, "upload_transition");
                activity.startActivity(i, optionsCompat.toBundle());
            }
        });
    }

    @Override
    public int getItemCount() {
        return datas.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView title;
        TextView subtitle;
        TextView version;
        Button upload;
        View body;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title_swp);
            subtitle = itemView.findViewById(R.id.subtitle_swp);
            version = itemView.findViewById(R.id.version_swp);
            upload = itemView.findViewById(R.id.upload_swp);
            body = itemView.findViewById(R.id.body_swp);
        }
    }
}