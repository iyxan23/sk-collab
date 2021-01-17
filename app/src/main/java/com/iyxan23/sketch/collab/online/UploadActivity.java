package com.iyxan23.sketch.collab.online;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
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

        int projectId = getIntent().getIntExtra("projectid", -1);

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

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();

        uploadButton.setOnClickListener(v -> {
            DatabaseReference projectReference = database.getReference("/" + (isPrivate.isChecked() ? "userprojects/" + auth.getUid() + "/projects" : "projects"));
            String pushKey = projectReference.push().getKey();
            DatabaseReference commitsReference = database.getReference("/" + (isPrivate.isChecked() ? "userprojects/" + auth.getUid() + "/commits" : "commits"));

            // Nullcheck
            if (pushKey == null) {
                Toast.makeText(UploadActivity.this, "An error occured: pushKey is null", Toast.LENGTH_LONG).show();
                return;
            }

            // Add a custom key to the sketchware project named "sk-colab-key": "(pushkey)" and "sk-collab-owner": "(uid)"
            try {
                swProj.mysc_project = Util.encrypt(
                        new JSONObject(
                                Util.decrypt(swProj.mysc_project)
                        )
                                .put("sk-collab-key", pushKey)  // The pushkey (location of the project)
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

            HashMap<String, Object> data = new HashMap<String, Object>() {{
                put("name", name.getText());
                put("description", description.getText());
                put("author", auth.getUid());
                put("version", 1);
                put("open", isOpenSource.isChecked());
                put("snapshot",
                    new HashMap<String, Object>() {{
                        put("commit_id", "initial");
                        put("file", Util.base64encode(swProj.file));
                        put("library", Util.base64encode(swProj.library));
                        put("logic", Util.base64encode(swProj.logic));
                        put("project", Util.base64encode(swProj.mysc_project));
                        put("resource", Util.base64encode(swProj.resource));
                        put("view", Util.base64encode(swProj.view));
                    }}
                );
            }};

            HashMap<String, Object> commit_data = new HashMap<String, Object>() {{
                put("author", auth.getUid());
                put("timestamp", System.currentTimeMillis());
            }};


            ProgressDialog progressDialog = new ProgressDialog(UploadActivity.this);
            progressDialog.setTitle("Uploading project");
            progressDialog.setMessage("Uploading " + name.getText() + ", please wait");
            progressDialog.show();
            progressDialog.setIndeterminate(true);

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
        });
    }
}
