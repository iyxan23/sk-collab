package com.iyxan23.sketch.collab.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.iyxan23.sketch.collab.R;
import com.iyxan23.sketch.collab.models.BrowseItem;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;


public class BrowseItemAdapter extends RecyclerView.Adapter<BrowseItemAdapter.ViewHolder> {
    private static final String TAG = "BrowseItemAdapter";

    private ArrayList<BrowseItem> datas = new ArrayList<>();
    // final private Activity activity;
    WeakReference<Activity> activity;

    public BrowseItemAdapter(Activity activity) {
        this.activity = new WeakReference<>(activity);
    }

    public BrowseItemAdapter(ArrayList<BrowseItem> datas, Activity activity) {
        this.datas = datas;
        this.activity = new WeakReference<>(activity);
    }

    public void updateView(ArrayList<BrowseItem> datas) {
        this.datas = datas;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(
                LayoutInflater
                        .from(parent.getContext())
                        .inflate(R.layout.rv_item_browse, parent, false)
        );
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        Log.d(TAG, "onBindViewHolder: called.");
        BrowseItem item = datas.get(position);

        holder.title.setText(item.username + "/" + item.project_name);

        // https://stackoverflow.com/questions/25710457/how-to-subtract-two-calendar-object-in-android
        // TODO: IMPLEMENT A BETTER VERSION OF THIS THING
        Calendar now = Calendar.getInstance();
        long difference = now.getTimeInMillis() - item.latest_commit_timestamp;
        holder.last_updated.setText("Last Updated " + (int) (difference / (1000 * 60 * 60 * 24)) + " days ago");
    }

    @Override
    public int getItemCount() {
        return datas.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView title;
        TextView last_updated;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.item_browse_title);
            last_updated = itemView.findViewById(R.id.item_browse_last_updated);
        }
    }
}
