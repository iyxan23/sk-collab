package com.iyxan23.sketch.collab;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Pair;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.view.ViewCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Source;
import com.iyxan23.sketch.collab.models.SketchwareProject;
import com.iyxan23.sketch.collab.models.SketchwareProjectChanges;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

class MainActivity extends AppCompatActivity {

    // project keys
    // Pair<project_key, "project" file>
    ArrayList<Pair<String, JSONObject>> publicProjectsOwned = new ArrayList<>();
    ArrayList<Pair<String, JSONObject>> userProjects = new ArrayList<>();

    ArrayList<SketchwareProject> localProjects = new ArrayList<>();
    ArrayList<SketchwareProject> sketchcollabProjects = new ArrayList<>();

    ArrayList<SketchwareProjectChanges> changes = new ArrayList<>();

    FirebaseFirestore database = FirebaseFirestore.getInstance();
    FirebaseAuth auth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Get rid of the flasing white color while doing shared element transition
        getWindow().setEnterTransition(null);
        getWindow().setExitTransition(null);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Check if Read and Write external storage permission is granted
        // why scoped storage? :(
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED ||
            ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {

            // Tell the user first of why you need to grant the permission
            Toast.makeText(this, "We need storage permission to access your sketchware projects. SketchCollab can misbehave if you denied the permission.", Toast.LENGTH_LONG).show();
            // Reuest the permission(s)
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    100);
        } else {
            // Permission is already granted, initialize
            initialize();
        }

        /*
         * I don't think these are nessecary
         *
        // Get user projects, and projects that the user collaborates
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        FirebaseAuth auth  = FirebaseAuth.getInstance();

        DatabaseReference userProjectsRef = database.getReference("/userprojects/" + auth.getUid());
        CollectionReference userProjectsRef_ = firestore.collection("userdata").document(auth.getUid()).collection("projects");
        DatabaseReference projectsRef = database.getReference("/projects/" + auth.getUid());
        CollectionReference projectsRef_ = firestore.collection("/projects/");

        projectsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                for (DataSnapshot project : snapshot.getChildren()) {
                    if (Objects.equals(project.child("author").getValue(String.class), auth.getUid())) {
                        String projectBase64 = project.child("snapshot").child("project").getValue(String.class);
                        JSONObject json;

                        try {
                            json = new JSONObject(Util.base64decode(projectBase64));
                        } catch (JSONException e) {
                            e.printStackTrace();

                            // Looks like someone uploaded a broken JSON, skip
                            continue;
                        }

                        // Because project id can vary on different devices
                        if (json.has("sc_id")) json.optInt("sc_id");

                        Pair<String, JSONObject> pair = new Pair<>(project.getKey(), json);
                        publicProjectsOwned.add(pair);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                Toast.makeText(MainActivity.this,
                            "Error while fetching data: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

        userProjectsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                for (DataSnapshot project : snapshot.getChildren()) {
                    String projectBase64 = project.child("snapshot").child("project").getValue(String.class);

                    JSONObject json;
                    try {
                        json = new JSONObject(Util.base64decode(projectBase64));
                    } catch (JSONException e) {
                        e.printStackTrace();

                        // Looks like someone uploaded a broken JSON, skip
                        continue;
                    }

                    // Because project id can vary on different devices
                    if (json.has("sc_id")) json.optInt("sc_id");

                    Pair<String, JSONObject> pair = new Pair<>(project.getKey(), json);
                    userProjects.add(pair);
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                Toast.makeText(MainActivity.this,
                                "Error while fetching data: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
         */

        // OnClicks
        findViewById(R.id.projects_main).setOnClickListener(v -> {

            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                    MainActivity.this,
                    findViewById(R.id.imageView8),
                    Objects.requireNonNull( ViewCompat.getTransitionName(findViewById(R.id.imageView8)) )
            );

            Intent intent = new Intent(MainActivity.this,SketchwareProjectsListActivity.class);
            startActivity(intent, options.toBundle());
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == PackageManager.PERMISSION_GRANTED) {
                initialize();
            }
        }
    }

    private void initialize() {
        new Thread(() -> {
            // Fetch sketchware projects
            localProjects = Util.fetch_sketchware_projects();

            // Get sketchcollab projects
            for (SketchwareProject project: localProjects) {
                try {
                    // Is this project a sketchcollab project?
                    if (project.isSketchCollabProject()) {
                        // Alright add it to the arraylist
                        sketchcollabProjects.add(project);
                    }
                } catch (JSONException e) {
                    // Hmm weird, user's project is corrupted
                    e.printStackTrace();
                }
            }

            // Check if there isn't any sketchcollab projects
            if (sketchcollabProjects.size() == 0)
                // Ight ima head out
                return;

            for (SketchwareProject project: sketchcollabProjects) {
                try {
                    // Check every project if the project has changed yet
                    // Get the latest project commit, project visibility, and the author
                    String project_commit = project.getSketchCollabLatestCommitID();
                    String project_key = project.getSketchCollabKey();
                    boolean is_project_public = project.isSketchCollabProjectPublic();
                    String author = project.getSketchCollabAuthorUid();

                    if (!author.equals(auth.getUid())) {
                        // Hmm, the user "stole" another user's project
                        // Let's skip this one :e_sweat_smile:
                        // =========================================================================
                        // Anyway, don't worry the user cannot edit the data in the database, it's
                        // protected by the firebase firestore rules.

                        continue;
                    }

                    // Fetch the latest project commit in the database
                    Task<QuerySnapshot> task = database.collection(is_project_public ? "projects" : "userdata/" + author + "/projects").document(project_key).collection("commits")
                            .orderBy("timestamp", Query.Direction.DESCENDING) // Order by the timestamp
                            .limit(1) // I just wanted the latest commit, not every commit
                            .get(Source.SERVER) // Don't get the cache :/
                            .addOnCompleteListener(t -> { });

                    // Wait for the task to finish, i don't want to query a lot of tasks in a short amount of time
                    QuerySnapshot snapshot = Tasks.await(task);

                    // Check if task is successful or not
                    if (task.isSuccessful()) {
                        assert snapshot != null; // snapshot shouldn't be null

                        DocumentSnapshot commit_info = snapshot.getDocuments().get(0);

                        if (!project_commit.equals(commit_info.getId())) {
                            // Hmm, looks like this man's project has an older commit, tell him to update his project
                        } else {
                            // This mans project has the same commit
                            // Check if this project also has the same shasum
                            try {
                                String local_shasum = project.sha512sum();
                                String server_shasum = commit_info.getString("sha512sum");

                                // Check if they're the same
                                if (!local_shasum.equals(server_shasum)) {
                                    // Alright looks like he's got some local updates with the same head commit

                                    // Fetch the project
                                    SketchwareProject head_project = new SketchwareProject();

                                    Task<QuerySnapshot> project_data = database.collection(is_project_public ? "projects" : "userdata/" + author + "/projects").document(project_key).collection("snapshot")
                                            .get(Source.SERVER) // Don't get the cache :/
                                            .addOnCompleteListener(t -> { });

                                    // Wait for the task to finish, i don't want to query a lot of tasks in a short amount of time,
                                    // it can cause some performance issues
                                    QuerySnapshot project_data_snapshot = Tasks.await(project_data);

                                    // Loop through the document and get every data/ files
                                    for (DocumentSnapshot doc_snapshot: project_data_snapshot.getDocuments()) {
                                        if (doc_snapshot.getId().equals("logic")) {
                                            head_project.logic = doc_snapshot.getBlob("data").toBytes();

                                        } else if (doc_snapshot.getId().equals("view")) {
                                            head_project.view = doc_snapshot.getBlob("data").toBytes();

                                        } else if (doc_snapshot.getId().equals("file")) {
                                            head_project.file = doc_snapshot.getBlob("data").toBytes();

                                        } else if (doc_snapshot.getId().equals("mysc_project")) {
                                            head_project.mysc_project = doc_snapshot.getBlob("data").toBytes();

                                        } else if (doc_snapshot.getId().equals("library")) {
                                            head_project.library = doc_snapshot.getBlob("data").toBytes();

                                        } else if (doc_snapshot.getId().equals("resource")) {
                                            head_project.resource = doc_snapshot.getBlob("data").toBytes();
                                        }
                                    }

                                    // Add this to the changed sketchcollab sketchware projects arraylist
                                    changes.add(new SketchwareProjectChanges(project, head_project));

                                } /* else {
                                    // Boom, it's the same project with no updates
                                    // ight ima head out
                                } */
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Toast.makeText(MainActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    } else {
                        Toast.makeText(MainActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }

                } catch (JSONException | InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}