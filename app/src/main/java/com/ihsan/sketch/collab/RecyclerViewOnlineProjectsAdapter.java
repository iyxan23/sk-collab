package com.ihsan.sketch.collab;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;


public class RecyclerViewOnlineProjectsAdapter extends RecyclerView.Adapter<RecyclerViewOnlineProjectsAdapter.ViewHolder> implements Filterable {
    private static final String TAG = "OnlineProjsAdapter";

    private ArrayList<OnlineProject> datas;
    private ArrayList<OnlineProject> datas_full;
    private Activity activity;

    public RecyclerViewOnlineProjectsAdapter(ArrayList<OnlineProject> datas, Activity activity) {
        this.datas = datas;
        this.activity = activity;
        this.datas_full = datas;
    }

    public void updateView(ArrayList<OnlineProject> datas) {
        this.datas = datas;
        this.datas_full = datas;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item_online_projs, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        Log.d(TAG, "onBindViewHolder: called.");
        final OnlineProject project = datas.get(position);
        holder.title.setText(project.getTitle());
        holder.version.setText(project.getVersionProject());
        holder.author.setText(project.getAuthor());
        //holder.desc.setText(project.getDescription());
        holder.isopensourced.setTextColor(activity.getResources().getColor(project.isopen ? R.color.colorPrimary : R.color.colorAccent));
        holder.isopensourced.setText(project.getOpen());
        holder.desc.setText(project.getDescription());

        holder.body.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(activity, ViewOnlineProjectActivity.class);
            }
        });
    }

    @Override
    public int getItemCount() {
        return datas.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView title;
        TextView version;
        TextView author;
        TextView isopensourced;
        TextView desc;
        View body;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.proj_title);
            author = itemView.findViewById(R.id.proj_author);
            version = itemView.findViewById(R.id.proj_version);
            isopensourced = itemView.findViewById(R.id.proj_is_open_source);
            body = itemView.findViewById(R.id.proj_body);
        }
    }

    @Override
    public Filter getFilter() {
        return projsFilter;
    }

    private Filter projsFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            ArrayList<OnlineProject> filtered = new ArrayList<>();

            if (charSequence == null || charSequence.length() == 0) {
                filtered.addAll(datas_full);
            } else {
                String pattern = charSequence.toString().toLowerCase().trim();

                for (OnlineProject onlineProject : datas_full) {
                    if (onlineProject.getTitle().contains(pattern)) {
                        filtered.add(onlineProject);
                    }
                }
            }

            FilterResults results = new FilterResults();
            results.values = filtered;

            Log.d(TAG, "performFiltering: " + filtered.toString());

            return results;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            datas.clear();
            datas.addAll((ArrayList) filterResults.values);
            Log.d(TAG, "publishResults: " + filterResults.toString());
            notifyDataSetChanged();
        }
    };
}