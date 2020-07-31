package com.ihsan.sketch.collab;

import androidx.annotation.LongDef;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class UploadActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);
        getWindow().setEnterTransition(new Slide());
        getWindow().setExitTransition(new Explode());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        TextView no_proj = findViewById(R.id.no_sw_proj_detected_upload);
        final Spinner spinner = findViewById(R.id.spinner_sw_proj_upload);
        final Switch private_switch = findViewById(R.id.private_upload);
        final Switch open_source_switch = findViewById(R.id.opensource_upload);

        final Button upload = findViewById(R.id.upload_upload);
        final Button cancel = findViewById(R.id.cancel_upload);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        final FirebaseUser user = auth.getCurrentUser();
        assert user != null;

        ArrayList<SketchwareProject> swprojs = Util.getSketchwareProjects();

        if (swprojs.size() == 0) {
            upload.setEnabled(false);
            private_switch.setEnabled(false);
            open_source_switch.setEnabled(false);
        } else {
            no_proj.setVisibility(View.GONE);
            ArrayAdapter<SketchwareProject> spinnerArrayAdapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_dropdown_item, swprojs);
            spinner.setAdapter(spinnerArrayAdapter);
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

                ProgressBar uploadProgress = findViewById(R.id.progressUpload);
                TextView status = findViewById(R.id.status_upload);
                TextInputLayout textInputLayout = findViewById(R.id.desc_upload);
                String desc = textInputLayout.getEditText().getText().toString().trim();

                uploadProgress.setVisibility(View.VISIBLE);
                status.setVisibility(View.VISIBLE);
                uploadProgress.setProgress(0);
                status.setText("Waiting to upload..");
                upload.setEnabled(false);
                cancel.setEnabled(false);

                try {
                    SketchwareProject project = (SketchwareProject) spinner.getSelectedItem();
                    String data_path = Environment.getExternalStorageDirectory() + "/.sketchware/data/" + project.id + "/";
                    Log.e("UplaodActivity", "onClick: " + data_path);

                    RandomAccessFile file = null;
                    file = new RandomAccessFile(data_path + "file", "r");
                    byte[] bArr = new byte[((int) file.length())];
                    file.readFully(bArr);
                    String file_ = new String(bArr);

                    file = new RandomAccessFile(data_path + "view", "r");
                    byte[] bArr1 = new byte[((int) file.length())];
                    file.readFully(bArr1);
                    String view_ = new String(bArr1);

                    file = new RandomAccessFile(data_path + "logic", "r");
                    byte[] bArr2 = new byte[((int) file.length())];
                    file.readFully(bArr2);
                    String logic_ = new String(bArr2);

                    file = new RandomAccessFile(data_path + "resource", "r");
                    byte[] bArr3 = new byte[((int) file.length())];
                    file.readFully(bArr3);
                    String resource = new String(bArr3);

                    file = new RandomAccessFile(data_path + "library", "r");
                    byte[] bArr4 = new byte[((int) file.length())];
                    file.readFully(bArr4);
                    String library = new String(bArr4);

                    SketchwareProjectData swdata = new SketchwareProjectData(file_, view_, resource, logic_, library, project);

                    // Empty the variables to reduce memory usage
                    file_ = null;
                    bArr = null;
                    view_ = null;
                    bArr1 = null;
                    resource = null;
                    bArr2 = null;
                    logic_ = null;
                    bArr3 = null;
                    library = null;
                    bArr4 = null;

                    // Start uploading
                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    String uploadLocation = isPublic ? "/projects/" : "/userdata/" + user.getUid() + "/projects/";
                    DatabaseReference ref = database.getReference(uploadLocation);
                    String key = ref.push().getKey();
                    assert key != null;

                    HashMap<String, Object> data = new HashMap<>();
                    HashMap<String, Object> snapshot = new HashMap<>();
                    data.put("author", user.getUid());
                    data.put("name", project.name);
                    data.put("version", Double.valueOf(project.version));
                    data.put("open", isOpenSource);
                    data.put("desc", desc);

                    snapshot.put("file", swdata.getEncryptedFile());
                    snapshot.put("view", swdata.getEncryptedView_());
                    snapshot.put("resource", swdata.getEncryptedResource());
                    snapshot.put("logic", swdata.getEncryptedLogic());
                    snapshot.put("library", swdata.getEncryptedLibrary());
                    data.put("snapshot", snapshot);
                    ref.child(key).updateChildren(data)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    success();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    dieError(e.getMessage());
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
        finish();
    }

    private void dieError(String error) {
        findViewById(R.id.progressUpload).setVisibility(View.GONE);
        findViewById(R.id.status_upload).setVisibility(View.GONE);
        Snackbar.make(findViewById(R.id.container_upload), error, Snackbar.LENGTH_LONG)
                .setBackgroundTint(Color.RED)
                .show();

    }
}