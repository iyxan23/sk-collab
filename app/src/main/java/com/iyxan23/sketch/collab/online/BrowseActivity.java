package com.iyxan23.sketch.collab.online;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.iyxan23.sketch.collab.R;
import com.iyxan23.sketch.collab.adapters.BrowseItemAdapter;
import com.iyxan23.sketch.collab.models.BrowseItem;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

public class BrowseActivity extends AppCompatActivity {

    // Used to cache uid -> names
    HashMap<String, String> cached_names = new HashMap<>();

    FirebaseFirestore firestore;
    CollectionReference projects;
    CollectionReference userdata;

    ArrayList<BrowseItem> items = new ArrayList<>();

    BrowseItemAdapter adapter;

    // This variable is used to point at the bottom of our fetches
    DocumentReference after;

    // This variable indicates if we're at the bottom of the fetch (or that we've basically fetched every projects), not the recyclerview
    boolean is_at_bottom = false;

    // The project count that should be loaded
    int project_count_should_be_loaded = 5;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse);

        RecyclerView rv = findViewById(R.id.browse_rv);
        rv.setLayoutManager(new LinearLayoutManager(this));

        adapter = new BrowseItemAdapter(this);
        rv.setAdapter(adapter);

        if (savedInstanceState != null) {
            if (!savedInstanceState.isEmpty()) {
                adapter.updateView(savedInstanceState.getParcelableArrayList("items"));
                
                findViewById(R.id.progressBar_browse).setVisibility(View.GONE);

                return;
            }
        }

        firestore = FirebaseFirestore.getInstance();
        projects = firestore.collection("projects");
        userdata = firestore.collection("userdata");

        new Thread(() -> {
            // Only show open source projects
            load_projects(project_count_should_be_loaded);

            if (savedInstanceState != null) savedInstanceState.putParcelableArrayList("items", items);

            runOnUiThread(() -> {
                findViewById(R.id.progressBar_browse).setVisibility(View.GONE);
                adapter.updateView(items);

                // Listener to listen if we're at the bottom of the list, if it is, load more stuff
                rv.addOnScrollListener(new RecyclerView.OnScrollListener() {
                    @Override
                    public void onScrollStateChanged(@NotNull RecyclerView recyclerView, int newState) {
                        super.onScrollStateChanged(recyclerView, newState);

                        if (!recyclerView.canScrollVertically(1) && newState == RecyclerView.SCROLL_STATE_IDLE) {
                            // We're at the bottom, load more projects!
                            load_projects(project_count_should_be_loaded);
                        }
                    }
                });
            });
        }).start();
    }

    private void load_projects(int count) {
        Log.d("BrowseActivity", "load_projects: called.");
        new Thread(() -> {
            // Check if we're at the bottom
            if (is_at_bottom)
                // Don't load more projects :doggo_cheems:
                return;

            Query fetch_projects_query = projects
                                            .orderBy("name")
                                            .whereEqualTo("open", true)
                                            .limit(count);

            // Check if the pointer has been set (if it hasn't then this is the first time we call this function)
            if (after != null) fetch_projects_query.startAt(after);

            Task<QuerySnapshot> task = fetch_projects_query.get();

            try {
                Log.d("BrowseActivity", "Waiting for query");
                Tasks.await(task);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(BrowseActivity.this, "An error occured while fetching data: " + task.getException().getMessage(), Toast.LENGTH_LONG).show());
            }

            if (!task.isSuccessful()) {
                runOnUiThread(() -> Toast.makeText(BrowseActivity.this, "An error occured while retrieving data: " + task.getException().getMessage(), Toast.LENGTH_LONG).show());
                return;
            }

            int counter = 0;    // This counter is used if we've reached to the bottom of the projects list
                                // and if we did, don't load more projects

            for (DocumentSnapshot project: task.getResult().getDocuments()) {
                counter++;

                Log.d("BrowseActivity", "At loop: " + counter);

                String username;
                String uid_uploader = project.getString("author");

                if (cached_names.containsKey(uid_uploader)) {
                    username = cached_names.get(uid_uploader);

                } else {
                    // Fetch it's username
                    Task<DocumentSnapshot> userdata_fetch = userdata.document(uid_uploader).get();

                    try {
                        Tasks.await(userdata_fetch);

                        if (!userdata_fetch.isSuccessful()) {
                            runOnUiThread(() -> Toast.makeText(BrowseActivity.this, "Error while fetching userdata: " + userdata_fetch.getException().getMessage(), Toast.LENGTH_LONG).show());

                            return;
                        }

                        DocumentSnapshot user = userdata_fetch.getResult();

                        username = user.getString("name");

                        cached_names.put(uid_uploader, username);

                    } catch (ExecutionException | InterruptedException e) {
                        e.printStackTrace();
                        runOnUiThread(() -> Toast.makeText(BrowseActivity.this, "Error while fetching userdata: " + e.getMessage(), Toast.LENGTH_LONG).show());

                        return;
                    }
                }

                // Get the latest commit timestamp
                Timestamp latest_commit_timestamp;

                // Add backwards compatibility for the old ealry-alpha version
                if (!project.contains("latest_commit_timestamp")) {
                    latest_commit_timestamp = Timestamp.now();
                } else {
                    latest_commit_timestamp = project.getTimestamp("latest_commit_timestamp");
                }

                items.add(
                        new BrowseItem(
                                project.getId(),
                                username,
                                project.getString("name"),
                                uid_uploader,
                                latest_commit_timestamp
                        )
                );

                Log.d("BrowseActivity", "load_projects: UpdateView");
                runOnUiThread(() -> adapter.updateView(items));

                // Set the pointer of the last project loaded
                after = project.getReference();
            }

            // Check if we're at the bottom (means we've laoded less projects than we should've been)
            if (counter < count)
                is_at_bottom = true;
        }).start();
    }
}
