package com.iyxan23.sketch.collab.pickers;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Tasks;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.iyxan23.sketch.collab.R;
import com.iyxan23.sketch.collab.Util;
import com.iyxan23.sketch.collab.adapters.ChangesAdapter;
import com.iyxan23.sketch.collab.models.SketchwareProjectChanges;
import com.iyxan23.sketch.collab.online.PushCommitActivity;

import org.json.JSONException;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class UserPicker extends AppCompatActivity {

    List<DocumentSnapshot> users;

    FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    CollectionReference users_collection = firestore.collection("userdata");

    // ArrayList of Pair of Name and UID
    ArrayList<Pair<String, String>> selected_users = new ArrayList<>();

    RecyclerView users_rv;
    UserAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_picker);

        Intent intent = getIntent();

        boolean multiple_users = intent.getBooleanExtra("multiple_users", false);

        ExtendedFloatingActionButton efab = findViewById(R.id.button2);

        users_rv = findViewById(R.id.recycler_view_user_picker);

        // Shrink / expand the ExtendedFloatingActionButton according to the scroll activity
        // https://stackoverflow.com/questions/56822412/shrink-and-extend-function-on-new-extendedfloatingactionbutton-in-material-compo
        users_rv.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    efab.extend();
                }

                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0 || dy < 0 && efab.isExtended()) {
                    efab.shrink();
                }
            }
        });

        // Run these in a new thread
        new Thread(() -> {
            try {
                // Load users
                load_users();

                // And bind the view, (set recyclerview adapter, update some stuff)
                bind_views();
            } catch (ExecutionException | InterruptedException e) {
                runOnUiThread(() -> Toast.makeText(this, "Error while fetching: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        });
    }

    private void load_users() throws ExecutionException, InterruptedException {
        QuerySnapshot res = Tasks.await(users_collection.get());
        users = res.getDocuments();
    }

    private void bind_views() {
        adapter = new UserAdapter((ArrayList<DocumentSnapshot>) users, this);
        users_rv.setLayoutManager(new LinearLayoutManager(this));
    }

    public void done_button_click(View view) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("selected_users", selected_users);
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }

    // The UserAdapter used for the UsersRecyclerView
    public static class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {
        private static final String TAG = "UserAdapter";

        private ArrayList<DocumentSnapshot> datas = new ArrayList<>();
        WeakReference<Activity> activity;

        public UserAdapter(Activity activity) {
            this.activity = new WeakReference<>(activity);
        }

        public UserAdapter(ArrayList<DocumentSnapshot> datas, Activity activity) {
            this.datas = datas;
            this.activity = new WeakReference<>(activity);
        }

        public void updateView(ArrayList<DocumentSnapshot> datas) {
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
            DocumentSnapshot item = datas.get(position);
            // TODO: THIS
        }

        @Override
        public int getItemCount() {
            return datas.size();
        }

        public static class ViewHolder extends RecyclerView.ViewHolder {

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                // TODO: DESIGN A RECYCLERVIEW ITEM LAYOUT
            }
        }
    }
}