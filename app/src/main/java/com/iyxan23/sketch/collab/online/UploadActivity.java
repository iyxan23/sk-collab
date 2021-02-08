package com.iyxan23.sketch.collab.online;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
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
import com.google.firebase.firestore.WriteBatch;
import com.iyxan23.sketch.collab.R;
import com.iyxan23.sketch.collab.Util;
import com.iyxan23.sketch.collab.models.SketchwareProject;
import com.iyxan23.sketch.collab.models.Userdata;
import com.iyxan23.sketch.collab.pickers.UserPicker;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
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

        members_list = findViewById(R.id.members_upload);

        Button uploadButton = findViewById(R.id.upload_upload);
        SwitchMaterial isPrivate = findViewById(R.id.private_upload);
        SwitchMaterial isOpenSource = findViewById(R.id.open_source_upload);

        EditText description = findViewById(R.id.description_upload);
        EditText name = findViewById(R.id.name_upload);

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();

        isPrivate.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Private project cannot be open source
            isOpenSource.setChecked(!isChecked);
            isOpenSource.setEnabled(!isChecked);

            if (isChecked) {
                findViewById(R.id.add_member_button).setEnabled(false);
                members_list.setText("Members are disabled. To add members, make the project to be public.");

                members_list.animate().setDuration(500).alpha(0.25f);
                findViewById(R.id.members_title).animate().setDuration(500).alpha(0.25f);

                members.clear();
            } else {
                findViewById(R.id.add_member_button).setEnabled(true);
                members_list.setText("None, click the Add button to add Member(s).");

                members_list.animate().setDuration(500).alpha(1f);
                findViewById(R.id.members_title).animate().setDuration(500).alpha(1f);
            }
        });

        uploadButton.setOnClickListener(v -> {
            CollectionReference projectRef = firestore.collection("/" + (isPrivate.isChecked() ? "userdata/" + auth.getUid() + "/projects" : "projects"));

            WriteBatch upload_batch = firestore.batch();

            HashMap<String, Object> data = new HashMap<String, Object>() {{
                put("name", name.getText().toString());
                put("description", description.getText().toString());
                put("author", auth.getUid());
                put("version", 1);
                put("open", isOpenSource.isChecked());
                put("latest_commit_timestamp", Timestamp.now());
            }};

            // Check if the user has added any members yet
            if (!members.isEmpty()) {
                // Put all of those members into one String arraylist
                ArrayList<String> members_ = new ArrayList<>();

                for (int i = 0; i < members.size(); i++) {
                    members_.add(members.get(i).getUid());
                }

                // Put it in the project root
                data.put("members", members_);
            }

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

            DocumentReference projectRefDoc = projectRef.document();
            CollectionReference snapshotRef = projectRefDoc.collection("logic");
            CollectionReference commitRef = projectRefDoc.collection("commits");

            // Upload the project metadata
            upload_batch.set(projectRefDoc, data);

            // Upload project datas ================================================================
            upload_batch.set(
                    snapshotRef.document("logic"),
                    new HashMap<String, Object>() {{
                        put("data", Blob.fromBytes(swProj.logic));
                        put("shasum", Util.sha512(swProj.logic));
                    }}
            );

            upload_batch.set(
                    snapshotRef.document("view"),
                    new HashMap<String, Object>() {{
                        put("data", Blob.fromBytes(swProj.view));
                        put("shasum", Util.sha512(swProj.view));
                    }}
            );

            upload_batch.set(
                    snapshotRef.document("file"),
                    new HashMap<String, Object>() {{
                        put("data", Blob.fromBytes(swProj.file));
                        put("shasum", Util.sha512(swProj.file));
                    }}
            );

            upload_batch.set(
                    snapshotRef.document("library"),
                    new HashMap<String, Object>() {{
                        put("data", Blob.fromBytes(swProj.library));
                        put("shasum", Util.sha512(swProj.library));
                    }}
            );

            upload_batch.set(
                    snapshotRef.document("resource"),
                    new HashMap<String, Object>() {{
                        put("data", Blob.fromBytes(swProj.resource));
                        put("shasum", Util.sha512(swProj.resource));
                    }}
            );

            upload_batch.set(
                    snapshotRef.document("mysc_project"),
                    new HashMap<String, Object>() {{
                        put("data", Blob.fromBytes(swProj.mysc_project));
                        put("shasum", Util.sha512(swProj.mysc_project));
                    }}
            );
            // Upload project datas ===============================================================/

            // Upload the first initial commit data
            upload_batch.set(commitRef.document("initial"), commit_data);

            // Commit the upload!
            upload_batch
                    .commit()
                    .addOnCompleteListener(task -> {
                        Toast.makeText(UploadActivity.this, "Project Uploaded, refresh the homepage to see your project.", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(UploadActivity.this, "An error occured: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        progressDialog.dismiss();
                    });

            /*
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
                                        Toast.makeText(UploadActivity.this, "An error occured while uploading: " + task1.getException().getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                });
                    });
             */
        });
    }

    public void show_help(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Help");
        builder.setMessage(
                Html.fromHtml(
                    "<b>Member</b>:" +
                    "<p>Members are users that can directly make changes to your project. This can include your Team, Friends, Partner, and etc.</p>" +
                    "<p>NOTE: Non-Member(s) can also make changes to your project. But, You / Your project's member(s) will need to review the changes manually</p><br/>" +

                    "<b>Open Source:</b>" +
                    "<p>Enabled: People can view your project and your project's source code, only selected member(s) can make changes.</p>" +
                    "<p>Disabled: People cannot view or view your project's source code, only selected member(s) can view / make changes to your project.</p><br/>" +

                    "<b>Private:</b>" +
                    "<p>Enabled: Only YOU can view / edit the project, members are disabled in this private mode.</p>" +
                    "<p>Disabled: People can view / make changes / contribute to your project depending if it's open source or not.</p><br/>" +

                    "<p>Still need help? Ask it on <a href=\"https://github.com/Iyxan23/sk-collab/issues\">https://github.com/Iyxan23/sk-collab/issues</a></p>"
                )
        );

        builder.create().show();
    }

    final int MEMBER_USER_PICK_REQ_CODE = 20;
    ArrayList<Userdata> members = new ArrayList<>();

    TextView members_list;

    public void add_member_click(View view) {
        startActivityForResult(new Intent(this, UserPicker.class), MEMBER_USER_PICK_REQ_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == MEMBER_USER_PICK_REQ_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                members = data.getParcelableArrayListExtra("selected_users");

                boolean is_first = false;
                for (Userdata userdata: members) {
                    members_list.setText((!is_first ? "" : members_list.getText() + ", ") + userdata.getName());

                    is_first = true;
                }
            }
        }
    }
}
