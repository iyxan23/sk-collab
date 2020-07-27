package com.ihsan.sketch.collab;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;


public class OnlineProjsAdapter extends RecyclerView.Adapter<OnlineProjsAdapter.ViewHolder> {
    private static final String TAG = "OnlineProjsAdapter";

    private ArrayList<OnlineProject> datas;

    public OnlineProjsAdapter(ArrayList<OnlineProject> datas) {
        this.datas = datas;
    }

    public void updateView(ArrayList<OnlineProject> datas) {
        this.datas = datas;
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
        OnlineProject project = datas.get(position);
        holder.title.setText(project.getTitle());
        holder.version.setText(project.getVersionProject());
        holder.author.setText(project.getAuthor());
        //holder.desc.setText(project.getDescription());
        holder.isopensourced.setText(project.getOpen());

        holder.body.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

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
        View bot;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.proj_title);
            author = itemView.findViewById(R.id.proj_author);
            version = itemView.findViewById(R.id.proj_version);
            isopensourced = itemView.findViewById(R.id.proj_is_open_source);
            body = itemView.findViewById(R.id.proj_body);
        }
    }

}