package com.ihsan.sketch.collab;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;

public class RecyclerViewAuthorAdapter extends RecyclerView.Adapter<RecyclerViewAuthorAdapter.ViewHolder> {

    private ArrayList<HashMap<String, Object>> data;

    public RecyclerViewAuthorAdapter(ArrayList<HashMap<String, Object>> data) {
        this.data = data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.author_recyclerview_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.authorName.setText((String) data.get(position).get("name"));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView authorName;
        View parent;

        public ViewHolder(View itemView) {
            super(itemView);
            authorName = itemView.findViewById(R.id.name_author_item);
            parent = itemView.findViewById(R.id.parent_author);
        }
    }
}
