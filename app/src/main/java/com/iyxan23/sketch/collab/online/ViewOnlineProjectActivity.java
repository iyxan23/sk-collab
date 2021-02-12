package com.iyxan23.sketch.collab.online;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.iyxan23.sketch.collab.R;
import com.iyxan23.sketch.collab.Util;
import com.iyxan23.sketch.collab.databinding.ActivityViewOnlineProjectBinding;
import com.iyxan23.sketch.collab.models.SketchwareProject;
import com.iyxan23.sketch.collab.models.Userdata;
import com.iyxan23.sketch.collab.services.CloneService;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class ViewOnlineProjectActivity extends AppCompatActivity {

    private static final String TAG = "ViewOnlineProjectActivi";

    private ActivityViewOnlineProjectBinding binding;

    private String description_;

    String project_key;
    String project_name;

    HashMap<String, String> cached_names = new HashMap<>();

    ArrayList<Userdata> members = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityViewOnlineProjectBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Toolbar toolbar = binding.toolbar;

        setSupportActionBar(toolbar);

        Objects.requireNonNull(getSupportActionBar());

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        CollapsingToolbarLayout toolbar_layout = findViewById(R.id.toolbar_layout);

        final FloatingActionButton fab_edit = binding.fabEdit;
        final ExtendedFloatingActionButton fab_fork = binding.fabFork;

        project_key = getIntent().getStringExtra("project_key");

        // TODO: CREATE A GENERATED IMAGE USING CANVAS IF THE PROJECT'S BANNER IS EMPTY

        // Author edits the project
        // The user can only click this fab if he is the author (check line 78)
        fab_edit.setOnClickListener(v -> {
            Intent i = new Intent(this, EditProjectActivity.class);
            i.putExtra("description", description_);
            i.putExtra("project_key", project_key);
            i.putExtra("members", members);
            startActivity(i);
        });

        // Fork the project
        fab_fork.setOnClickListener(v -> {
            // TODO: IMPLEMENT FORKING PROJECTS
        });

        // Fetch the project from the database
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        DocumentReference project = firestore.collection("projects").document(project_key);
        CollectionReference project_commits = firestore.collection("projects").document(project_key).collection("commits");
        CollectionReference userdata = firestore.collection("userdata");

        String user_uid = auth.getUid();

        final DocumentSnapshot[] tmp = new DocumentSnapshot[3];

        project
                .get()
                .continueWithTask(task -> {
                    tmp[0] = task.getResult();
                    return userdata.document(tmp[0].getString("author")).get();
                })
                .continueWithTask(task -> {
                    tmp[1] = task.getResult();

                    // Hide / Show these FABs
                    // Check if this user is the owner / author of this project
                    if (user_uid.equals(tmp[0].getString("author"))) {
                        // Yup, He's the owner
                        // Show the edit fab
                        fab_edit.setVisibility(View.VISIBLE);
                    } else {
                        // Nop, he's a visitor
                        // Show the fork fab
                        fab_fork.setVisibility(View.VISIBLE);
                    }

                    // Get the latest commit
                    return project_commits.orderBy("timestamp", Query.Direction.DESCENDING).limit(1).get();
                })
                /* Currently not implemented, because the first commit should be initial
                .continueWithTask(task -> {
                    tmp[2] = task.getResult().getDocuments().get(0);

                    // Get the first commit
                    return project_commits.orderBy("timestamp", Query.Direction.ASCENDING).limit(1).get();
                })
                 */
                .addOnSuccessListener(result -> {
                    Log.d(TAG, "onCreate: Fetch success");
                    
                    DocumentSnapshot project_data = tmp[0];
                    DocumentSnapshot uploader_userdata = tmp[1];
                    DocumentSnapshot latest_commit = result.getDocuments().get(0);

                    TextView commit_end = findViewById(R.id.commit_end);
                    TextView commit_end_id = findViewById(R.id.commit_end_id);
                    TextView commit_start = findViewById(R.id.commit_start);
                    TextView commit_start_id = findViewById(R.id.commit_start_id);
                    TextView app_title = findViewById(R.id.app_title_project);
                    TextView description_textview = findViewById(R.id.description_project);

                    String name = project_data.getString("name");
                    String description = project_data.getString("description");
                    String author_name = uploader_userdata.getString("name");
                    String latest_commit_id = latest_commit.getId();
                    String latest_commit_message = latest_commit.getString("name");
                    String first_commit_id = "initial";
                    String first_commit_message = "Initial Commit";

                    List<String> members_ = (List<String>) project_data.get("members");
                    // Aahhh, we need to fetch the member's usernames, k hold on

                    project_name = name;
                    description_ = description;

                    // Set these to the views
                    app_title.setText(author_name + "/" + name);
                    toolbar_layout.setTitle(name);
                    commit_end.setText(latest_commit_message);
                    commit_end_id.setText(latest_commit_id);
                    commit_start.setText(first_commit_message);
                    commit_start_id.setText(first_commit_id);
                    description_textview.setText(description);

                    Log.d(TAG, "onCreate: Fetching members");
                    fetchMembers(members_);
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error while fetching: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void fetchMembers(List<String> members_) {
        Log.d(TAG, "fetchMembers: members_: " + members_);

        // Check if members_ is null bruh
        if (members_ == null) {
            // kekw we're outta here
            return;
        }

        CollectionReference userdata = FirebaseFirestore.getInstance().collection("userdata");

        new Thread(() -> {
            for (String uid : members_) {
                String username;
                if (cached_names.containsKey(uid)) {
                    username = cached_names.get(uid);

                    Log.d(TAG, "fetchMembers: Username already cached: " + uid + "; value: " + username);

                } else {
                    // Fetch it's username
                    Task<DocumentSnapshot> userdata_fetch = userdata.document(uid).get();

                    try {
                        Tasks.await(userdata_fetch);

                        if (!userdata_fetch.isSuccessful()) {
                            runOnUiThread(() -> Toast.makeText(this, "Error while fetching userdata: " + userdata_fetch.getException().getMessage(), Toast.LENGTH_LONG).show());

                            return;
                        }

                        DocumentSnapshot user = userdata_fetch.getResult();

                        username = user.getString("name");

                        Log.d(TAG, "fetchMembers: Username fetched, uid: " + uid + ", name: " + username);

                        cached_names.put(uid, username);

                    } catch (ExecutionException | InterruptedException e) {
                        e.printStackTrace();
                        runOnUiThread(() -> Toast.makeText(this, "Error while fetching userdata: " + e.getMessage(), Toast.LENGTH_LONG).show());

                        return;
                    }
                }

                Log.d(TAG, "fetchMembers: Add member: " + username + " (" + uid + ")");
                members.add(new Userdata(username, uid));
            }

            // Update the textview
            runOnUiThread(() -> {
                TextView members_list = findViewById(R.id.members_list);
                members_list.setText("");

                boolean is_start = false;

                for (Userdata member_userdata: members) {
                    members_list.append((is_start ? ", " : "") + member_userdata.getName());

                    is_start = true;
                }

                // Hide the progressbar
                findViewById(R.id.progress_project).setVisibility(View.GONE);
            });
        }).start();
    }

    // onClick for the "Browse Code" button
    public void browseCodeOnClick(View v) {
        Intent i = new Intent(this, BrowseCodeActivity.class);
        i.putExtra("project_key", project_key);
        i.putExtra("project_name", project_name);
        startActivity(i);
    }

    // onClick for the "Commits" button
    public void commitsOnClick(View v) {
        Intent i = new Intent(this, CommitsActivity.class);
        i.putExtra("project_key", project_key);
        i.putExtra("project_name", project_name);
        startActivity(i);
    }

    // onClick for the "Clone" button
    public void cloneOnClick(View v) {
        AlertDialog.Builder exists_dialog_builder = new AlertDialog.Builder(this);
        exists_dialog_builder.setCancelable(true);
        exists_dialog_builder.setTitle("Duplicate Project");
        exists_dialog_builder.setMessage("This project already exists in your device (ID: " + project_key + "), are you sure you want to continue?");
        exists_dialog_builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        exists_dialog_builder.setPositiveButton("Yes", (dialog, which) -> {
            dialog.dismiss();
            do_clone();
        });

        AlertDialog exists_dialog = exists_dialog_builder.create();

        new Thread(() -> {
            boolean already_exists = false;
            ArrayList<SketchwareProject> projects = Util.fetch_sketchware_projects();

            for (SketchwareProject project : projects) {
                try {
                    String key = project.getSketchCollabKey();

                    if (key.equals(project_key)) {
                        // There's already a project
                        already_exists = true;

                        break;
                    }
                } catch (JSONException ignore) { }
            }

            if (already_exists) {
                runOnUiThread(exists_dialog::show);
            } else {
                // Clone right away
                // Probably will add a confirmation dialog
                do_clone();
            }
        }).start();
    }

    private void do_clone() {
        Intent cloneService = new Intent(this, CloneService.class);
        cloneService.putExtra("project_key", project_key);
        cloneService.putExtra("project_name", project_name);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(cloneService);
        } else {
            startService(cloneService);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}