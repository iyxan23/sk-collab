package com.iyxan23.sketch.collab;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Pair;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.view.ViewCompat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import com.iyxan23.sketch.collab.models.SketchwareProject;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;


class MainActivity extends AppCompatActivity {

    // project keys
    // Pair<project_key, "project" file>
    ArrayList<Pair<String, JSONObject>> publicProjectsOwned = new ArrayList<>();
    ArrayList<Pair<String, JSONObject>> userProjects = new ArrayList<>();

    ArrayList<SketchwareProject> localProjects = new ArrayList<>();

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
        }

        // Get user projects, and projects that the user collaborates
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        FirebaseAuth auth  = FirebaseAuth.getInstance();
        DatabaseReference userProjectsRef = database.getReference("/userprojects/" + auth.getUid());
        DatabaseReference projectsRef = database.getReference("/projects/" + auth.getUid());


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

                // Fetch sketchware projects
                new Thread(() -> {
                    localProjects = Util.fetch_sketchware_projects();

                    SharedPreferences sp = getSharedPreferences("data", MODE_PRIVATE);
                    JSONObject sp_json = null;

                    if (!sp.contains("last_changes") && sp.getString("last_changes", "").equals("")) {
                        sp_json = new JSONObject();

                        // Hash every sketchware projects and save them into this field
                        for (SketchwareProject project: localProjects) {
                            try {
                                if (!project.isSketchCollabProject())
                                    continue;

                                String shasum = project.sha512sum();

                                sp_json.put(project.getSketchCollabKey(), shasum);
                            } catch (JSONException e) {
                                // Hmm, weird
                                e.printStackTrace();
                            }
                        }

                        sp.edit().putString("last_changes", sp_json.toString()).apply();
                    } else {
                        try {
                            sp_json = new JSONObject(sp.getString("last_changes", ""));

                            for (SketchwareProject project: localProjects) {
                                if (!project.isSketchCollabProject())
                                    continue;

                                // Check if the hash doesn't match
                                String shasum_before = sp_json.getString(project.getSketchCollabKey());
                                String shasum_now = project.sha512sum();

                                if (!shasum_now.equals(shasum_before)) {
                                    // Add to the changes list

                                }
                            }
                        } catch (JSONException e) {
                            // Very weird execption
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        }
    }
}