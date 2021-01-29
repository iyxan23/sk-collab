package com.iyxan23.sketch.collab.online;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.iyxan23.sketch.collab.R;
import com.iyxan23.sketch.collab.adapters.CommitAdapter;
import com.iyxan23.sketch.collab.models.Commit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class CommitsActivity extends AppCompatActivity {

    HashMap<String, String> cached_usernames = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_commits);

        String project_key = getIntent().getStringExtra("project_key");

        // One liner go brrr
        ((TextView) findViewById(R.id.commits_project_name)).setText("on " + getIntent().getStringExtra("project_name"));

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        CollectionReference commits_reference = firestore.collection("projects").document(project_key).collection("commits");

        RecyclerView commit_rv = findViewById(R.id.commits_rv);
        CommitAdapter adapter = new CommitAdapter(this);

        commit_rv.setLayoutManager(new LinearLayoutManager(this));
        commit_rv.setAdapter(adapter);

        ArrayList<Commit> commits_ = new ArrayList<>();

        commits_reference
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    new Thread(() -> {
                        if (!task.isSuccessful()) {
                            Toast.makeText(this, "Error while fetching commits: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            return;
                        }

                        QuerySnapshot commits = task.getResult();

                        for (QueryDocumentSnapshot commit : commits) {
                            String username;
                            String author_uid = commit.getString("uid");

                            if (!cached_usernames.containsKey(commit.getString("uid"))) {
                                // Fetch the username

                                DocumentReference userdata = firestore.collection("userdata").document(author_uid);

                                try {
                                    DocumentSnapshot userdata_snapshot = Tasks.await(userdata.get());
                                    username = userdata_snapshot.getString("name");

                                    cached_usernames.put(author_uid, username);

                                } catch (ExecutionException | InterruptedException e) {
                                    e.printStackTrace();
                                    Toast.makeText(this, "Error while fetching userdata: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                    return;
                                }

                            } else {
                                username = cached_usernames.get(author_uid);
                            }

                            Commit c = new Commit();

                            c.id = commit.getId();
                            c.author_username = username;
                            c.author = commit.getString("author");
                            c.name = commit.getString("name");
                            c.sha512sum = commit.getString("sha512sum");
                            c.patch = commit.get("patch", Map.class);
                            c.timestamp = commit.getTimestamp("timestamp");

                            commits_.add(c);
                            runOnUiThread(() -> adapter.updateView(commits_));
                        }

                        Log.d(TAG, "onCreate: Finished, exiting");
                        runOnUiThread(() -> findViewById(R.id.progressBar_commits).setVisibility(View.GONE));
                    }).start();
        });
    }
}