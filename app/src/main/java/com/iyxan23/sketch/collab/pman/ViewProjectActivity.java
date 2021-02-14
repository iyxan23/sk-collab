package com.iyxan23.sketch.collab.pman;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.ImageViewCompat;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Source;
import com.iyxan23.sketch.collab.R;
import com.iyxan23.sketch.collab.Util;
import com.iyxan23.sketch.collab.helpers.OnlineProjectHelper;
import com.iyxan23.sketch.collab.models.SketchwareProject;
import com.iyxan23.sketch.collab.models.SketchwareProjectChanges;
import com.iyxan23.sketch.collab.online.UploadActivity;
import com.iyxan23.sketch.collab.online.ViewOnlineProjectActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import static com.iyxan23.sketch.collab.helpers.OnlineProjectHelper.hasPermission;
import static com.iyxan23.sketch.collab.helpers.OnlineProjectHelper.querySnapshotToSketchwareProject;

public class ViewProjectActivity extends AppCompatActivity {

    private static final String TAG = "ViewProjectActivity";

    SketchwareProject project;
    boolean is_sketchcollab_project;

    FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    FirebaseAuth auth = FirebaseAuth.getInstance();

    String project_commit;
    String project_key;
    boolean is_project_public;
    String author;

    SketchwareProjectChanges changes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_project);

        Intent intent = getIntent();

        // Get the project extra
        project = intent.getParcelableExtra("project");

        if (project.metadata == null) {
            finish_corrupted();
        }

        TextView project_name   = findViewById(R.id.project_name_vp);
        TextView app_name       = findViewById(R.id.app_name);
        TextView id             = findViewById(R.id.project_id);

        project_name    .setText(project.metadata.project_name);
        app_name        .setText(project.metadata.app_name);
        id              .setText(String.valueOf(project.metadata.id));

        // Is this a SketchCollab Project?
        try {
            is_sketchcollab_project = project.isSketchCollabProject();
        } catch (JSONException e) {
            finish_corrupted();
        }

        if (!is_sketchcollab_project) {
            // Change the UI if this isn't a SketchCollab project,
            // No need to do the opposite, the opposite is already in the UI itself
            findViewById(R.id.sketchcollab_project_indicator).setVisibility(View.GONE);

            ((TextView) findViewById(R.id.textView24)).setTextColor(0xFF555555);
            ImageViewCompat.setImageTintList(findViewById(R.id.imageView15), ColorStateList.valueOf(0xFF555555));

            ((TextView) findViewById(R.id.textView25)).setTextColor(0xFF555555);
            ImageViewCompat.setImageTintList(findViewById(R.id.imageView16), ColorStateList.valueOf(0xFF555555));

            ((TextView) findViewById(R.id.textView26)).setTextColor(0xFFFFFFFF);
            ImageViewCompat.setImageTintList(findViewById(R.id.imageView17), ColorStateList.valueOf(0xFFFFFFFF));
        } else {
            // This is a sketchcollab project, initialize some stuff
            try {
                project_commit = project.getSketchCollabLatestCommitID();
                project_key = project.getSketchCollabKey();
                is_project_public = project.isSketchCollabProjectPublic();
                author = project.getSketchCollabAuthorUid();

            } catch (JSONException e) {
                e.printStackTrace();

                finish_corrupted();
            }
        }
    }

    private void fetch_changes() {
        new Thread(() -> {
            // Fetch the project data
            Task<DocumentSnapshot> task_proj =
                    firestore.collection(is_project_public ? "projects" : "userdata/" + author + "/projects").document(project_key)
                            .get(Source.SERVER); // Don't get the cache :/

            DocumentSnapshot project_data;

            try {
                project_data = Tasks.await(task_proj);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
                return;
            }

            // Check if task is successful or not
            if (!task_proj.isSuccessful()) {
                assert task_proj.getException() != null; // Exception shouldn't be null if the task is not successful

                Toast.makeText(this, "Error: " + task_proj.getException().getMessage(), Toast.LENGTH_LONG).show();
                return;
            }

            assert project_data != null; // This shouldn't be null

            // Don't go further if we don't have a permission to make changes in it.
            if (!hasPermission(project_data)) {
                return;
            }

            // Fetch the latest project commit in the database
            Task<QuerySnapshot> task =
                    firestore.collection(is_project_public ? "projects" : "userdata/" + author + "/projects").document(project_key).collection("commits")
                            .orderBy("timestamp", Query.Direction.DESCENDING) // Order by the timestamp
                            .limit(1) // I just wanted the latest commit, not every commit
                            .get(Source.SERVER); // Don't get the cache :/

            // Wait for the task to finish
            QuerySnapshot commit_snapshot;

            try {
                commit_snapshot = Tasks.await(task);

            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();

                return;
            }

            // Check if task is successful or not
            if (!task.isSuccessful()) {
                assert task.getException() != null; // Exception shouldn't be null if the task is not successful

                Toast.makeText(this, "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                return;
            }

            assert commit_snapshot != null; // snapshot shouldn't be null

            Log.d(TAG, "documents: " + commit_snapshot.getDocuments());
            Log.d(TAG, "project_key: " + project_key);

            // Check if the project doesn't exists in the database.
            if (commit_snapshot.getDocuments().size() == 0) return;

            DocumentSnapshot commit_info = commit_snapshot.getDocuments().get(0);

            if (!project_commit.equals(commit_info.getId())) {
                // Hmm, looks like this man's project has an older commit, tell him to update his project
                Log.d(TAG, "initialize: Old Commit");
            } else {
                Log.d(TAG, "initialize: Latest commit");
                // This mans project has the same commit
                // Check if this project also has the same shasum
                String local_shasum;

                try {
                    local_shasum = project.sha512sum();
                } catch (JSONException e) {
                    e.printStackTrace();

                    return;
                }

                String server_shasum = commit_info.getString("sha512sum");

                Log.d(TAG, "initialize: Checking shasum");
                Log.d(TAG, "shasum local:  " + local_shasum);
                Log.d(TAG, "shasum server: " + server_shasum);

                // Check if they're the same
                if (!local_shasum.equals(server_shasum)) {
                    // Alright looks like he's got some local updates with the same head commit

                    // Get the project with the latest commit
                    SketchwareProject head_project;

                    try {
                        head_project = (SketchwareProject) OnlineProjectHelper.fetch_project_latest_commit(project_key);
                    } catch (ExecutionException | InterruptedException e) {
                        e.printStackTrace();

                        return;
                    }

                    // Set this as the changes
                    changes = new SketchwareProjectChanges(project, head_project);

                    // Update the UI
                    runOnUiThread(() -> {

                    });
                }
            }
        }).start();
    }

    private void finish_corrupted() {
        Toast.makeText(this, "This project is corrupted", Toast.LENGTH_SHORT).show();

        finish();
    }

    public void back(View view) {
        onBackPressed();
    }

    public void openOnlineProject(View view) {
        // Only execute this if this is a sketchcollab project
        if (is_sketchcollab_project) {
            Intent i = new Intent(this, ViewOnlineProjectActivity.class);

            try {
                i.putExtra("project_key", project.getSketchCollabKey());
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to get project key, this project is possibly corrupted.", Toast.LENGTH_SHORT).show();

                return;
            }

            startActivity(i);
        }
    }

    public void removeReference(View view) {
        // Only execute this if this is a sketchcollab project
        if (is_sketchcollab_project) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Confirmation");
            builder.setMessage("This will remove the reference of your sketchcollab project, diconnecting the project from the server. You can though, clone the project to get a project with the reference to it, but this is permanent for this project");
            builder.setPositiveButton("Yes", (dialog, which) -> {
                // Initiate the Intercontential Ballistic Missile
                // jk

                try {
                    // Remove the references / keys
                    JSONObject project_json = new JSONObject(Util.decrypt(project.mysc_project));
                    project_json.remove("sk-collab-key");
                    project_json.remove("sk-collab-owner");
                    project_json.remove("sk-collab-latest-commit");
                    project_json.remove("sk-collab-project-visibility");

                    // then put it back
                    project.mysc_project = Util.encrypt(project_json.toString().getBytes());

                    // and then apply it
                    project.applyChanges();

                    // Ok done, time to refresh the activity
                    recreate();

                    // Oh ye, just in case, close the dialog
                    dialog.dismiss();

                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "An error occured: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });

            builder.setNegativeButton("Cancel", (dialog, which) -> {
                // ight ima head out
                dialog.dismiss();
            });

            // Build and Show the dialog!
            builder.create().show();
        }
    }

    public void uploadProject(View view) {
        // Only execute if this isn't a sketchcollab project
        if (!is_sketchcollab_project) {
            // Open up UploadActivity
            Intent i = new Intent(this, UploadActivity.class);

            i.putExtra("project_id", project.metadata.id);
            // Note: project.metadata will never be null because we already checked it in onCreate

            // Alright, let's fire it up!
            startActivity(i);
        }
    }
}