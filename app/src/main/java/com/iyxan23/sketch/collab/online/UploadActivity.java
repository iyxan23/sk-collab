package com.iyxan23.sketch.collab.online;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.Blob;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.iyxan23.sketch.collab.R;
import com.iyxan23.sketch.collab.Util;
import com.iyxan23.sketch.collab.models.SketchwareProject;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;

public class UploadActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        int projectId = getIntent().getIntExtra("project_id", -1);

        if (projectId == -1) {
            // No extras given
            // Exit instead
            finish();
            return;
        }

        // Get the sketchware project
        SketchwareProject swProj = Util.get_sketchware_project(projectId);

        if (swProj == null) {
            // Project doesn't exist
            // Exit instead
            finish();
            return;
        }

        // Set the TextView
        TextView projectName = findViewById(R.id.project_name_upload);
        projectName.setText(swProj.metadata.project_name);

        Button uploadButton = findViewById(R.id.upload_upload);
        SwitchMaterial isPrivate = findViewById(R.id.private_upload);
        SwitchMaterial isOpenSource = findViewById(R.id.open_source_upload);

        EditText description = findViewById(R.id.description_upload);
        EditText name = findViewById(R.id.name_upload);

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();

        uploadButton.setOnClickListener(v -> {
            // DatabaseReference projectReference = database.getReference("/" + (isPrivate.isChecked() ? "userprojects/" + auth.getUid() + "/projects" : "projects"));
            CollectionReference projectRef = firestore.collection("/" + (isPrivate.isChecked() ? "userdata/" + auth.getUid() + "/projects" : "projects"));
            // String pushKey = projectReference.push().getKey();
            // DatabaseReference commitsReference = database.getReference("/" + (isPrivate.isChecked() ? "userprojects/" + auth.getUid() + "/commits" : "commits"));

            // Nullcheck
            /*
            if (pushKey == null) {
                Toast.makeText(UploadActivity.this, "An error occured: pushKey is null", Toast.LENGTH_LONG).show();
                return;
            }
             */

            HashMap<String, Object> data = new HashMap<String, Object>() {{
                put("name", name.getText().toString());
                put("description", description.getText().toString());
                put("author", auth.getUid());
                put("version", 1);
                put("open", isOpenSource.isChecked());
                put("latest_commit_timestamp", Timestamp.now());
            }};

            try {
                data.put("sha512sum", swProj.sha512sum());
            } catch (JSONException e) {
                Toast.makeText(UploadActivity.this, "An error occured while doing shasum: " + e.getMessage(), Toast.LENGTH_LONG).show();
                return;
            }

            HashMap<String, Object> commit_data = new HashMap<String, Object>() {{
                put("author", auth.getUid());
                put("timestamp", Timestamp.now());
                put("name", "Initial Commit");
            }};

            try {
                commit_data.put("sha512sum", swProj.sha512sum());
            } catch (JSONException e) {
                Toast.makeText(UploadActivity.this, "An error occured while doing shasum: " + e.getMessage(), Toast.LENGTH_LONG).show();
                return;
            }

            ProgressDialog progressDialog = new ProgressDialog(UploadActivity.this);
            progressDialog.setTitle("Uploading project");
            progressDialog.setMessage("Uploading project metadata");
            progressDialog.setCancelable(false);
            progressDialog.setIndeterminate(true);
            progressDialog.show();

            projectRef
                    .add(data)
                    .addOnCompleteListener(task -> {
                        if (!task.isSuccessful()) {
                            // Failed
                            Toast.makeText(UploadActivity.this, "An error occured while uploading: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            progressDialog.dismiss();
                            return;
                        }

                        progressDialog.setMessage("Uploading project data");

                        // Get the result
                        DocumentReference res = task.getResult();

                        // Add a custom key to the sketchware project named "sk-colab-key": "(pushkey)" and "sk-collab-owner": "(uid)"
                        try {
                            swProj.mysc_project = Util.encrypt(
                                    new JSONObject(
                                            Util.decrypt(swProj.mysc_project)
                                    )
                                            .put("sk-collab-key", res.getId())  // The document id (location of the project)
                                            .put("sk-collab-owner", auth.getUid())  // The owner of the project (used to access private projects)
                                            .put("sk-collab-latest-commit", "initial")   // The latest commit id of this project
                                            .put("sk-collab-project-visibility", isPrivate.isChecked() ? "private" : "public")   // The project visibility. Don't worry, it's used to determine where is the project located in the database
                                            .toString()
                                            .getBytes()
                            );
                            swProj.applyChanges();
                        } catch (JSONException | IOException e) {
                            // This shouldn't happen
                            e.printStackTrace();
                            Toast.makeText(UploadActivity.this, "An error occured while applying the key: " + e.getMessage(), Toast.LENGTH_LONG).show();

                            // Stop
                            return;
                        }

                        // Upload the snapshot / project files
                        CollectionReference snapshotRef =
                                projectRef
                                        .document(res.getId())
                                        .collection("snapshot");

                        CollectionReference commitRef =
                                projectRef
                                        .document(res.getId())
                                        .collection("commits");

                        progressDialog.setMessage("Uploading logic");

                        // note: yes i know, this looks very dumb
                        snapshotRef

                                // Upload project files ============================================
                                // Upload data/logic
                                .document("logic")
                                .set(new HashMap<String, Object>() {{
                                    put("data", Blob.fromBytes(swProj.logic));
                                    put("shasum", Util.sha512(swProj.logic));
                                }}
                                )
                                // Upload data/view
                                .continueWithTask(unused -> {
                                        progressDialog.setMessage("Uploading view");
                                        return snapshotRef
                                                .document("view")
                                                .set(new HashMap<String, Object>() {{
                                                    put("data", Blob.fromBytes(swProj.view));
                                                    put("shasum", Util.sha512(swProj.view));
                                                }});
                                    }
                                )
                                // Upload data/file
                                .continueWithTask(unused -> {
                                        progressDialog.setMessage("Uploading file");
                                        return snapshotRef
                                                .document("file")
                                                .set(new HashMap<String, Object>() {{
                                                    put("data", Blob.fromBytes(swProj.file));
                                                    put("shasum", Util.sha512(swProj.file));
                                                }});
                                    }
                                )
                                // Upload data/resource
                                .continueWithTask(unused -> {
                                        progressDialog.setMessage("Uploading resource");
                                        return snapshotRef
                                                .document("resource")
                                                .set(new HashMap<String, Object>() {{
                                                    put("data", Blob.fromBytes(swProj.resource));
                                                    put("shasum", Util.sha512(swProj.resource));
                                                }});
                                    }
                                )
                                // Upload data/library
                                .continueWithTask(unused -> {
                                        progressDialog.setMessage("Uploading library");
                                        return snapshotRef
                                                .document("library")
                                                .set(new HashMap<String, Object>() {{
                                                    put("data", Blob.fromBytes(swProj.library));
                                                    put("shasum", Util.sha512(swProj.library));
                                                }});
                                    }
                                )
                                // Upload mysc/project
                                .continueWithTask(unused -> {
                                        progressDialog.setMessage("Uploading project");
                                        return snapshotRef
                                                .document("mysc_project")
                                                .set(new HashMap<String, Object>() {{
                                                    put("data", Blob.fromBytes(swProj.mysc_project));
                                                    put("shasum", Util.sha512(swProj.mysc_project));
                                                }});
                                    }
                                )
                                // Upload project files ============================================

                                // Upload the commit data ==========================================
                                .continueWithTask(unused -> {
                                        progressDialog.setMessage("Uploading the initial commit");
                                        return commitRef
                                                .document("initial")  // The first ever commit on a project is set with the ID "initial"
                                                .set(commit_data);
                                    }
                                )
                                // Upload the commit data ==========================================

                                // Done
                                .addOnCompleteListener(task1 -> {
                                    progressDialog.dismiss();

                                    if (task1.isSuccessful()) {
                                        // Alright, nice, destroy the activity and move on
                                        Toast.makeText(UploadActivity.this, "Project Uploaded, refresh the homepage to see your project.", Toast.LENGTH_SHORT).show();
                                        finish();
                                    } else {
                                        // Sad, it failed
                                        Toast.makeText(UploadActivity.this, "An error occured while uploading: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                });
                    });

            /*
            projectReference
                    .child(pushKey)
                    .setValue(data)
                    .addOnSuccessListener(unused ->
                            // Update the commit informations
                            commitsReference
                                .child(pushKey)
                                .child("initial")
                                .setValue(commit_data)
                                .addOnSuccessListener(unused1 -> {
                                    progressDialog.dismiss();

                                    Toast.makeText(UploadActivity.this, "Project Uploaded", Toast.LENGTH_LONG).show();
                                    finish();
                                }).addOnFailureListener(e -> {
                                    progressDialog.dismiss();
                                    Toast.makeText(UploadActivity.this, "An error occured: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                })
                    ).addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        Toast.makeText(UploadActivity.this, "An error occured: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });

             */
        });
    }

    public void show_help(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Help");
        builder.setMessage(
                "Open Source:\n" +
                "Enabled: People can view your project and your project's source code, only selected collaborators can contribute.\n" +
                "Disabled: People cannot view or view your project's source code, only selected collaborators can view / contribute to your project.\n\n" +

                "Private:\n" +
                "Enabled: Only YOU can view / edit the project, collaborators are disabled in this private mode.\n" +
                "Disabled: People can view / contribute to your project depending if it's open source or not.\n\n" +

                "Still need help? Ask it on https://github.com/Iyxan23/sk-collab/issues"
        );

        builder.create().show();
    }
}
