package com.iyxan23.sketch.collab.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.iyxan23.sketch.collab.models.SketchwareProject;
import com.iyxan23.sketch.collab.R;

import java.util.ArrayList;


public class SketchwareProjectAdapter extends RecyclerView.Adapter<SketchwareProjectAdapter.ViewHolder> {
    private static final String TAG = "OnlineProjsAdapter";

    private ArrayList<SketchwareProject> datas;

    public SketchwareProjectAdapter(ArrayList<SketchwareProject> datas, Activity activity) {
        this.datas = datas;
    }

    public void updateView(ArrayList<SketchwareProject> datas) {
        this.datas = datas;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_sketchware_project, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        Log.d(TAG, "onBindViewHolder: called.");
        SketchwareProject project = datas.get(position);
    }

    @Override
    public int getItemCount() {
        return datas.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView title;
        TextView subtitle;
        TextView details;
        Button upload_button;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.project_name);
            subtitle = itemView.findViewById(R.id.project_details);
            details = itemView.findViewById(R.id.details);
            upload_button = itemView.findViewById(R.id.upload_button);
        }
    }
}
