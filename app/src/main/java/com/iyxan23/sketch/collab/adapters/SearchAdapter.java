package com.iyxan23.sketch.collab.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.text.SpannableString;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.iyxan23.sketch.collab.R;
import com.iyxan23.sketch.collab.models.SearchItem;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder> {
    private static final String TAG = "BrowseItemAdapter";

    private ArrayList<SearchItem> datas = new ArrayList<>();
    WeakReference<Activity> activity;

    public SearchAdapter(Activity activity) {
        this.activity = new WeakReference<>(activity);
    }

    public SearchAdapter(ArrayList<SearchItem> datas, Activity activity) {
        this.datas = datas;
        this.activity = new WeakReference<>(activity);
    }

    public void updateView(ArrayList<SearchItem> datas) {
        this.datas = datas;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(
                LayoutInflater
                        .from(parent.getContext())
                        .inflate(R.layout.rv_search_item, parent, false)
        );
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        Log.d(TAG, "onBindViewHolder: called.");
        SearchItem item = datas.get(position);

        holder.title.setText(item.title);
        holder.subtitle.setText(item.subtitle);

        holder.body.setOnClickListener(v -> activity.get().startActivity(item.intent));
    }

    @Override
    public int getItemCount() {
        return datas.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView title;
        TextView subtitle;

        View body;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.search_item_title);
            subtitle = itemView.findViewById(R.id.search_item_subtitle);

            body = itemView.findViewById(R.id.search_body);
        }
    }
}
