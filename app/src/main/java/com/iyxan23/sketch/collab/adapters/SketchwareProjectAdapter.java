package com.iyxan23.sketch.collab.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.iyxan23.sketch.collab.R;
import com.iyxan23.sketch.collab.models.SketchwareProject;
import com.iyxan23.sketch.collab.online.UploadActivity;

import org.json.JSONException;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class SketchwareProjectAdapter extends RecyclerView.Adapter<SketchwareProjectAdapter.ViewHolder> {
    private static final String TAG = "SketchwareProjectAdaptr";

    private ArrayList<SketchwareProject> datas = new ArrayList<>();
    final private WeakReference<Activity> activity;

    public SketchwareProjectAdapter(Activity activity) {
        this.activity = new WeakReference<>(activity);
    }

    public SketchwareProjectAdapter(ArrayList<SketchwareProject> datas, Activity activity) {
        this.datas = datas;
        this.activity = new WeakReference<>(activity);
    }

    public void updateView(ArrayList<SketchwareProject> datas) {
        this.datas = datas;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_sketchware_project, parent, false));
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        Log.d(TAG, "onBindViewHolder: called.");
        SketchwareProject project = datas.get(position);

        holder.title.setText(project.metadata.app_name);
        holder.subtitle.setText(project.metadata.project_name);
        holder.details.setText(project.metadata.project_package + "(" + project.metadata.id + ")");

        try {
            if (project.isSketchCollabProject())
                holder.upload_button.setEnabled(false);

            else {
                holder.upload_button.setOnClickListener(v -> {
                    // Go to UploadActivity
                    try {
                        Intent i = new Intent(activity.get(), UploadActivity.class);
                        i.putExtra("project_id", project.getProjectID());
                        activity.get().startActivity(i);
                    } catch (JSONException e) {
                        Toast.makeText(activity.get(), "An error occured: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }

        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(activity.get(), "An error occured: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
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
            upload_button = itemView.findViewById(R.id.upload_button_rv);
        }
    }
}
