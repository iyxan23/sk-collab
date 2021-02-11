package com.iyxan23.sketch.collab.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.iyxan23.sketch.collab.R;
import com.iyxan23.sketch.collab.models.BrowseItem;
import com.iyxan23.sketch.collab.online.ViewOnlineProjectActivity;

import java.lang.ref.WeakReference;
import java.util.ArrayList;


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
        if (datas == null) {
            return;
        }

        if (datas.size() == 0) {
            return;
        }

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

        // https://stackoverflow.com/questions/11275034/android-calculating-minutes-hours-days-from-point-in-time
        CharSequence relativeTimeStr =
                DateUtils.getRelativeTimeSpanString(
                        item.latest_commit_timestamp.getSeconds() * 1000,
                        System.currentTimeMillis(),

                        DateUtils.SECOND_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE
                );

        holder.last_updated.setText("Last Updated " + relativeTimeStr);

        holder.body.setOnClickListener(v -> {
            // Move to ViewOnlineProjectActivity
            Intent i = new Intent(activity.get(), ViewOnlineProjectActivity.class);
            i.putExtra("project_key", item.project_id);
            activity.get().startActivity(i);
        });
    }

    @Override
    public int getItemCount() {
        return datas.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView title;
        TextView last_updated;

        View body;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.item_browse_title);
            last_updated = itemView.findViewById(R.id.item_browse_last_updated);
            body = itemView.findViewById(R.id.body_item_browse);
        }
    }
}
