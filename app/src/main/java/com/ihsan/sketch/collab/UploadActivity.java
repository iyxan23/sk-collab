package com.ihsan.sketch.collab;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.text.InputType;
import android.transition.Explode;
import android.transition.Slide;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;

public class UploadActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);
        getWindow().setEnterTransition(new Explode());
        getWindow().setExitTransition(new Explode());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        TextView no_proj = findViewById(R.id.no_sw_proj_detected_upload);
        final Spinner spinner = findViewById(R.id.spinner_sw_proj_upload);
        final Switch private_switch = findViewById(R.id.private_upload);
        final Switch open_source_switch = findViewById(R.id.opensource_upload);

        final Button upload = findViewById(R.id.upload_upload);
        final Button cancel = findViewById(R.id.cancel_upload);

        TextInputLayout textInputLayout = findViewById(R.id.desc_upload);
        textInputLayout.getEditText().setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        final FirebaseUser user = auth.getCurrentUser();
        assert user != null;

        ArrayList<SketchwareProject> swprojs = Util.getSketchwareProjects();

        // Cek kalo activity sebelumnya nunjuk ke projek
        Intent i = getIntent();

        if (swprojs.size() == 0) {
            upload.setEnabled(false);
            private_switch.setEnabled(false);
            open_source_switch.setEnabled(false);
        } else {
            no_proj.setVisibility(View.GONE);
            ArrayAdapter<SketchwareProject> spinnerArrayAdapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_dropdown_item, swprojs);
            spinner.setAdapter(spinnerArrayAdapter);
            spinner.setSelection(i.getIntExtra("swproj_index", 0));
        }

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean isPublic = !private_switch.isChecked();
                boolean isOpenSource = open_source_switch.isChecked();

                final ProgressBar uploadProgress = findViewById(R.id.progressUpload);
                final TextView status = findViewById(R.id.status_upload);
                TextInputLayout textInputLayout = findViewById(R.id.desc_upload);
                String desc = textInputLayout.getEditText().getText().toString().trim();

                uploadProgress.setVisibility(View.VISIBLE);
                status.setVisibility(View.VISIBLE);
                uploadProgress.setProgress(0);
                status.setText("Waiting to upload..");
                upload.setEnabled(false);
                cancel.setEnabled(false);

                try {
                    status.setText("Getting file path..");
                    SketchwareProject project = (SketchwareProject) spinner.getSelectedItem();
                    String data_path = Environment.getExternalStorageDirectory() + "/.sketchware/data/" + project.id + "/";
                    Log.e("UplaodActivity", "onClick: " + data_path);
                    uploadProgress.setProgress(10);

                    status.setText("Getting file");
                    RandomAccessFile file = null;
                    file = new RandomAccessFile(data_path + "file", "r");
                    byte[] bArr = new byte[((int) file.length())];
                    file.readFully(bArr);
                    String file_ = new String(bArr);
                    uploadProgress.setProgress(20);

                    status.setText("Getting view");
                    file = new RandomAccessFile(data_path + "view", "r");
                    byte[] bArr1 = new byte[((int) file.length())];
                    file.readFully(bArr1);
                    String view_ = new String(bArr1);
                    uploadProgress.setProgress(30);

                    status.setText("Getting logic");
                    file = new RandomAccessFile(data_path + "logic", "r");
                    byte[] bArr2 = new byte[((int) file.length())];
                    file.readFully(bArr2);
                    String logic_ = new String(bArr2);
                    uploadProgress.setProgress(40);

                    status.setText("Getting resource");
                    file = new RandomAccessFile(data_path + "resource", "r");
                    byte[] bArr3 = new byte[((int) file.length())];
                    file.readFully(bArr3);
                    String resource = new String(bArr3);
                    uploadProgress.setProgress(50);

                    status.setText("Getting library");
                    file = new RandomAccessFile(data_path + "library", "r");
                    byte[] bArr4 = new byte[((int) file.length())];
                    file.readFully(bArr4);
                    String library = new String(bArr4);
                    uploadProgress.setProgress(60);

                    status.setText("Getting project");
                    file = new RandomAccessFile(Environment.getExternalStorageDirectory().getAbsolutePath() + "/.sketchware/mysc/list/" + project.id + "/project", "r");
                    byte[] bArr5 = new byte[((int) file.length())];
                    file.readFully(bArr5);
                    String project_ = new String(bArr5);
                    uploadProgress.setProgress(60);

                    status.setText("Saving data to object");

                    status.setText("Starting to upload..");
                    // Start uploading
                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    String uploadLocation = isPublic ? "/projects/" : "/userdata/" + user.getUid() + "/projects/";
                    DatabaseReference ref = database.getReference(uploadLocation);
                    String key = ref.push().getKey();
                    assert key != null;
                    uploadProgress.setProgress(70);

                    status.setText("Placing data to the specified location");
                    HashMap<String, Object> data = new HashMap<>();
                    HashMap<String, Object> snapshot = new HashMap<>();
                    data.put("author", user.getUid());
                    data.put("name", project.name);
                    data.put("version", Double.valueOf(project.version));
                    data.put("open", isOpenSource);
                    data.put("desc", desc);
                    uploadProgress.setProgress(80);

                    status.setText("Getting project data");
                    snapshot.put("file", Util.base64encrypt(Util.decrypt(file_)));
                    snapshot.put("view", Util.base64encrypt(Util.decrypt(view_)));
                    snapshot.put("resource", Util.base64encrypt(Util.decrypt(resource)));
                    snapshot.put("logic", Util.base64encrypt(Util.decrypt(logic_)));
                    snapshot.put("library", Util.base64encrypt(Util.decrypt(library)));
                    snapshot.put("project", Util.base64encrypt(Util.decrypt(project_)));
                    data.put("snapshot", snapshot);
                    status.setText("Updating database");
                    uploadProgress.setProgress(90);
                    ref.child(key).updateChildren(data)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    uploadProgress.setProgress(100);
                                    status.setText("Success!");
                                    success();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    status.setText("Error occurred");
                                    dieError(e.getMessage());
                                    uploadProgress.setProgress(0);
                                }
                            });
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e("UPLOAD", "onClick: " + e.toString());
                    dieError(e.getMessage());
                }
            }
        });
    }

    private void success() {
        findViewById(R.id.progressUpload).setVisibility(View.GONE);
        findViewById(R.id.status_upload).setVisibility(View.GONE);
        Toast.makeText(this, "Success!", Toast.LENGTH_LONG).show();
        onBackPressed();
    }

    private void dieError(String error) {
        findViewById(R.id.progressUpload).setVisibility(View.GONE);
        findViewById(R.id.status_upload).setVisibility(View.GONE);
        Snackbar.make(findViewById(R.id.container_upload), error, Snackbar.LENGTH_LONG)
                .setBackgroundTint(Color.RED)
                .show();

    }
}