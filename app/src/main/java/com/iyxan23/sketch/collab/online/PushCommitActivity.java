package com.iyxan23.sketch.collab.online;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.iyxan23.sketch.collab.R;
import com.iyxan23.sketch.collab.Util;
import com.iyxan23.sketch.collab.models.SketchwareProjectChanges;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;

public class PushCommitActivity extends AppCompatActivity {

    SketchwareProjectChanges commit_change;

    TextView patch_text;

    HashMap<String, String> patch = new HashMap<>();
    boolean isReady = false;

    FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    CollectionReference commits;
    DocumentReference project;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_push_commit);

        // TODO: REALTIME CHECK IF THERE IS A NEW COMMIT WHILE THE USER IS IN THIS ACTIVITY
        // TODO: MERGE FUNCTIONALITY AND CHECKING

        commit_change = getIntent().getParcelableExtra("changes");
        String project_key = getIntent().getStringExtra("project_key");

        commits = firestore.collection("projects/" + project_key + "/commits");
        project = firestore.document("projects/" + project_key);

        patch_text = findViewById(R.id.patch_push);

        TextInputEditText title = findViewById(R.id.title_push_text);

        findViewById(R.id.push_button_push).setOnClickListener(v -> {
            // Check if it's ready or not
            // yeah, this check is _probably_ unnessecary, but just in case that the project
            // is so big and DiffMatchPatch took a very long time calculaing the patch
            if (!isReady) return;

            String user_uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

            HashMap<String, Object> data = new HashMap<String, Object>() {{
                put("name", title.getText().toString());
                put("timestamp", Timestamp.now());
                put("patch", patch);
                put("author", user_uid);
            }};

            Task<Void> update_last_commit_timestamp = project.update("latest_commit_timestamp", Timestamp.now());

            try {
                data.put("sha512sum", commit_change.after.sha512sum());
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(PushCommitActivity.this, "Error while doing shasum: " + e.getMessage(), Toast.LENGTH_LONG).show();
                return;
            }

            ProgressDialog progressDialog = new ProgressDialog(PushCommitActivity.this);
            progressDialog.setTitle("Uploading commit");
            progressDialog.setMessage("Please wait");
            progressDialog.setCancelable(false);
            progressDialog.setIndeterminate(true);
            progressDialog.show();

            update_last_commit_timestamp
                    .continueWithTask(task -> commits.add(data))
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Update the project's local commit id
                            JSONObject mysc_project = commit_change.after.getMyscProject();
                            try {
                                mysc_project.put("sk-collab-latest-commit", task.getResult().getId());
                                commit_change.after.mysc_project = Util.encrypt(mysc_project.toString().getBytes());
                                commit_change.after.applyChanges();
                            } catch (IOException | JSONException e) {
                                e.printStackTrace();
                                Toast.makeText(PushCommitActivity.this, "Error while doing commit: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                return;
                            }

                            // Alright!
                            progressDialog.dismiss();
                            Toast.makeText(PushCommitActivity.this, "Commit successful!", Toast.LENGTH_LONG).show();

                            PushCommitActivity.this.finish();
                        } else {
                            progressDialog.dismiss();
                            Toast.makeText(PushCommitActivity.this, "Error while doing commit: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        });

        findViewById(R.id.merge_button_push).setOnClickListener(v -> {

        });

        new Thread(() -> {
            // Get the patch and set it to the patch text thing

            // Get the changed files
            int files_changed = commit_change.getFilesChanged();

            // Pack every patches into one map
            int[] data_keys = new int[] {
                    SketchwareProjectChanges.LOGIC      ,
                    SketchwareProjectChanges.VIEW       ,
                    SketchwareProjectChanges.FILE       ,
                    SketchwareProjectChanges.LIBRARY    ,
                    SketchwareProjectChanges.RESOURCES  ,
            };

            String[] data_keys_str = new String[] {
                    "logic"      ,
                    "view"       ,
                    "file"       ,
                    "library"    ,
                    "resources"  ,
            };

            for (int index = 0; index < data_keys.length; index++) {
                if ((files_changed & data_keys[index]) == data_keys[index]) {
                    patch.put(data_keys_str[index], commit_change.getPatch(data_keys[index]));
                }
            }

            // Alright, patch is ready!
            isReady = true;

            // Put the patch in the TextView
            StringBuilder full_patch = new StringBuilder();

            for (String key: patch.keySet()) {
                full_patch.append(key).append("\n").append(patch.get(key)).append("\n");
            }

            patch_text.setText(full_patch.toString());
        }).start();
    }
}