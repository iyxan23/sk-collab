package com.iyxan23.sketch.collab.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.iyxan23.sketch.collab.R;
import com.iyxan23.sketch.collab.Util;
import com.iyxan23.sketch.collab.models.SketchwareProjectChanges;
import com.iyxan23.sketch.collab.online.PushCommitActivity;

import org.json.JSONException;

import java.lang.ref.WeakReference;
import java.util.ArrayList;


public class ChangesAdapter extends RecyclerView.Adapter<ChangesAdapter.ViewHolder> {
    private static final String TAG = "BrowseItemAdapter";

    private ArrayList<SketchwareProjectChanges> datas = new ArrayList<>();
    WeakReference<Activity> activity;

    public ChangesAdapter(Activity activity) {
        this.activity = new WeakReference<>(activity);
    }

    public ChangesAdapter(ArrayList<SketchwareProjectChanges> datas, Activity activity) {
        this.datas = datas;
        this.activity = new WeakReference<>(activity);
    }

    public void updateView(ArrayList<SketchwareProjectChanges> datas) {
        this.datas = datas;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(
                LayoutInflater
                        .from(parent.getContext())
                        .inflate(R.layout.rv_last_changes, parent, false)
        );
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        Log.d(TAG, "onBindViewHolder: called.");
        SketchwareProjectChanges item = datas.get(position);

        // TODO: EXTEND THIS
        holder.project_name.setText(item.before.metadata.project_name);
        holder.project_details.setText(item.before.metadata.project_package + " (" + item.before.metadata.id + ")");

        try {
            holder.details.setText("Local branch is at commit " + item.before.getSketchCollabLatestCommitID());
        } catch (JSONException e) {
            holder.details.setText("Error while retreiving commit ID, this project is possibly corrupted");
        }

        int files_changed = item.getFilesChanged();
        int addition = 0;
        int deletion = 0;

        int[] data_keys = new int[] {
                SketchwareProjectChanges.LOGIC      ,
                SketchwareProjectChanges.VIEW       ,
                SketchwareProjectChanges.FILE       ,
                SketchwareProjectChanges.LIBRARY    ,
                SketchwareProjectChanges.RESOURCES  ,
        };

        for (int data_key: data_keys) {
            if ((files_changed & data_key) == data_key) {
                int[] res = Util.getAdditionAndDeletion(item.getPatch(data_key));
                addition += res[0];
                deletion += res[1];
            }
        }

        holder.summary.setText("+" + addition + " -" + deletion);

        holder.addition.setMax(addition + deletion);
        holder.deletion.setMax(addition + deletion);

        holder.addition.setProgress(addition);
        holder.deletion.setProgress(deletion);

        holder.push_button.setOnClickListener(v -> {
            // Go to PushCommitActivity
            Intent i = new Intent(activity.get(), PushCommitActivity.class);
            i.putExtra("changes", item);

            try {
                i.putExtra("project_key", item.before.getSketchCollabKey());
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(activity.get(), "Error while getting project key: + " + e.getMessage(), Toast.LENGTH_LONG).show();
                return;
            }

            activity.get().startActivity(i);
        });
    }

    @Override
    public int getItemCount() {
        return datas.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView project_name;
        TextView project_details;
        TextView details;

        ProgressBar addition;
        ProgressBar deletion;

        Button push_button;

        TextView summary;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            project_name = itemView.findViewById(R.id.project_name);
            project_details = itemView.findViewById(R.id.project_details);
            details = itemView.findViewById(R.id.details);
            summary = itemView.findViewById(R.id.change_summary);
            addition = itemView.findViewById(R.id.addition);
            deletion = itemView.findViewById(R.id.deletion);
            push_button = itemView.findViewById(R.id.push_button);
        }
    }
}
