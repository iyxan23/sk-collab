package com.iyxan23.sketch.collab.online;

import android.os.Bundle;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.widget.TextView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.iyxan23.sketch.collab.R;
import com.iyxan23.sketch.collab.databinding.ActivityViewOnlineProjectBinding;

import org.jetbrains.annotations.NotNull;

public class ViewOnlineProjectActivity extends AppCompatActivity {

    private ActivityViewOnlineProjectBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityViewOnlineProjectBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        CollapsingToolbarLayout toolbar_layout = findViewById(R.id.toolbar_layout);

        final FloatingActionButton fab_edit = binding.fabEdit;
        final ExtendedFloatingActionButton fab_fork = binding.fabFork;

        // TODO: CREATE A GENERATED IMAGE USING CANVAS IF THE PROJECT'S BANNER IS EMPTY

        // Author edits the project
        // The user can only click this fab if he is the author (check line 78)
        fab_edit.setOnClickListener(v -> {

        });

        // Fork the project
        fab_fork.setOnClickListener(v -> {
            // TODO: IMPLEMENT FORKING PROJECTS
        });

        String project_key = getIntent().getStringExtra("project_key");

        // Fetch the project from the database
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        DocumentReference project = firestore.collection("projects").document(project_key);
        CollectionReference project_commits = firestore.collection("projects").document(project_key).collection("commits");
        CollectionReference userdata = firestore.collection("userdata");

        final DocumentSnapshot[] tmp = new DocumentSnapshot[3];

        project
                .get()
                .continueWithTask(task -> {
                    tmp[0] = task.getResult();
                    return userdata.document(tmp[0].getString("author")).get();
                })
                .continueWithTask(task -> {
                    tmp[1] = task.getResult();

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
                .addOnCompleteListener(task -> {
                    DocumentSnapshot project_data = tmp[0];
                    DocumentSnapshot uploader_userdata = tmp[1];
                    DocumentSnapshot latest_commit = task.getResult().getDocuments().get(0);

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

                    // Set these to the views
                    app_title.setText(author_name + "/" + name);
                    toolbar_layout.setTitle(name);
                    commit_end.setText(latest_commit_message);
                    commit_end_id.setText(latest_commit_id);
                    commit_start.setText(first_commit_message);
                    commit_start_id.setText(first_commit_id);
                    description_textview.setText(description);

                    // Hide the progressbar
                    findViewById(R.id.progress_project).setVisibility(View.GONE);
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}