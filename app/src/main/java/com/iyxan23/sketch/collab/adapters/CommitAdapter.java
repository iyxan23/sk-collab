package com.iyxan23.sketch.collab.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.iyxan23.sketch.collab.R;
import com.iyxan23.sketch.collab.models.BrowseItem;
import com.iyxan23.sketch.collab.models.Commit;
import com.iyxan23.sketch.collab.online.ViewOnlineProjectActivity;

import java.lang.ref.WeakReference;
import java.sql.Time;
import java.util.ArrayList;

public class CommitAdapter extends RecyclerView.Adapter<CommitAdapter.ViewHolder> {
    private static final String TAG = "CommitAdapter";

    // Should be the database's commit objects
    private ArrayList<Commit> datas = new ArrayList<>();
    // final private Activity activity;
    WeakReference<Activity> activity;

    public CommitAdapter(Activity activity) {
        this.activity = new WeakReference<>(activity);
    }

    public CommitAdapter(ArrayList<Commit> datas, Activity activity) {
        this.datas = datas;
        this.activity = new WeakReference<>(activity);
    }

    public void updateView(ArrayList<Commit> datas) {
        this.datas = datas;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(
                LayoutInflater
                        .from(parent.getContext())
                        .inflate(R.layout.rv_commit, parent, false)
        );
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        Log.d(TAG, "onBindViewHolder: called.");
        Commit item = datas.get(position);

        if (position == 0) {
            // First commit
            holder.icon.setImageDrawable(activity.get().getDrawable(R.drawable.ic_commit_start));

        } else if (position == datas.size() - 1) {
            // First commit
            holder.icon.setImageDrawable(activity.get().getDrawable(R.drawable.ic_commit_end));
        }

        holder.title.setText(item.name);
        holder.commit_id.setText(item.id);
        holder.author.setText(item.author_username);

        // https://stackoverflow.com/questions/11275034/android-calculating-minutes-hours-days-from-point-in-time
        CharSequence relativeTimeStr =
                DateUtils.getRelativeTimeSpanString(
                        item.timestamp.getNanoseconds() / 1000,
                        System.currentTimeMillis(),

                        DateUtils.SECOND_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE
                );

        holder.timestamp.setText(relativeTimeStr);

        holder.body.setOnClickListener(v -> {
            // Show a bottomsheet
            View bottom_sheet_view = LayoutInflater
                                            .from(v.getContext())
                                            .inflate(R.layout.bottomsheet_commit, null);

            BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(v.getContext());

            TextView title = bottom_sheet_view.findViewById(R.id.commit_title);
            TextView author = bottom_sheet_view.findViewById(R.id.commit_author);
            TextView code = bottom_sheet_view.findViewById(R.id.patch_code);
            TextView time = bottom_sheet_view.findViewById(R.id.commit_time);

            title.setText(item.name);
            author.setText(item.author_username + " (Commit ID: " + item.id + ")");

            StringBuilder patch = new StringBuilder();

            for (String key: item.patch.keySet()) {
                patch.append(key).append(":\n").append(item.patch.get(key));
            }

            code.setText(patch.toString());

            CharSequence relativeTimeStr_ =
                    DateUtils.getRelativeTimeSpanString(
                            item.timestamp.getNanoseconds() / 1000,
                            System.currentTimeMillis(),

                            DateUtils.SECOND_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE
                    );

            time.setText(relativeTimeStr_);

            bottomSheetDialog.setContentView(bottom_sheet_view);
            bottomSheetDialog.show();
        });
    }

    @Override
    public int getItemCount() {
        return datas.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView title;
        TextView author;
        TextView commit_id;
        TextView timestamp;

        ImageView icon;

        View body;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.rv_commit_title);
            author = itemView.findViewById(R.id.rv_commit_author);
            commit_id = itemView.findViewById(R.id.rv_commit_id);
            timestamp = itemView.findViewById(R.id.rv_commit_timestamp);

            icon = itemView.findViewById(R.id.rv_commit_icon);

            body = itemView.findViewById(R.id.commit_body);
        }
    }
}
