package com.ihsan.sketch.collab;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;


public class OnlineProjsAdapter extends RecyclerView.Adapter<OnlineProjsAdapter.ViewHolder> implements Filterable {
    private static final String TAG = "OnlineProjsAdapter";

    private ArrayList<OnlineProject> datas;
    private ArrayList<OnlineProject> datas_full;
    private Activity activity;

    public OnlineProjsAdapter(ArrayList<OnlineProject> datas, Activity activity) {
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

        holder.bot_layout.setVisibility(project.isExpanded ? View.VISIBLE : View.GONE);

        holder.body.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        holder.button_down.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OnlineProject proj = datas.get(position);
                proj.setExpanded(!project.isExpanded);
                datas.set(position, proj);
                holder.bot_layout.setVisibility(!project.isExpanded ? View.VISIBLE : View.GONE);
                holder.button_down.setImageDrawable(activity.getDrawable(!project.isExpanded ? R.drawable.ic_baseline_keyboard_arrow_up_24 : R.drawable.ic_baseline_keyboard_arrow_down_24));
                holder.isopensourced.setTextColor(activity.getResources().getColor(project.isopen ? R.color.colorPrimary : R.color.colorAccent));
                holder.isopensourced.setText(project.getOpen());
                notifyItemChanged(position);
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
        ImageView button_down;
        View body;
        View bot_top;
        View bot_layout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.proj_title);
            author = itemView.findViewById(R.id.proj_author);
            version = itemView.findViewById(R.id.proj_version);
            isopensourced = itemView.findViewById(R.id.proj_is_open_source);
            body = itemView.findViewById(R.id.proj_body);
            bot_top = itemView.findViewById(R.id.bot_top);
            bot_layout = itemView.findViewById(R.id.bot_layout);
            desc = itemView.findViewById(R.id.proj_desc);
            button_down = itemView.findViewById(R.id.proj_down);
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