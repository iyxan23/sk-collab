package com.ihsan.sketch.collab;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class CloneActivity extends AppCompatActivity {

    String project_key;
    String project_name;
    String project_author;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clone);

        getSupportActionBar().setTitle("Clone Project");

        Intent i = getIntent();
        project_key = i.getStringExtra("key");
        project_author = i.getStringExtra("author");
        project_name = i.getStringExtra("name");

        TextView txtview = findViewById(R.id.txtview_clone2);
        txtview.setText(project_name + " by " + project_author);

        Button clone = findViewById(R.id.btn_clone_clone);
        Button cancel = findViewById(R.id.btn_cancel_clone);

        clone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                findViewById(R.id.loading_overlay_clone).setVisibility(View.VISIBLE);
                cloneProject(project_key);
                finish();
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void cloneProject(String project_key) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference project_reference = database.getReference("projects/" + project_key);

        project_reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                byte[] file =       Util.encrypt(Util.base64decrypt(snapshot.child("snapshot").child("file").getValue(String.class)));
                byte[] view =       Util.encrypt(Util.base64decrypt(snapshot.child("snapshot").child("view").getValue(String.class)));
                byte[] resource =   Util.encrypt(Util.base64decrypt(snapshot.child("snapshot").child("resource").getValue(String.class)));
                byte[] logic =      Util.encrypt(Util.base64decrypt(snapshot.child("snapshot").child("logic").getValue(String.class)));
                byte[] library =    Util.encrypt(Util.base64decrypt(snapshot.child("snapshot").child("library").getValue(String.class)));
                byte[] project =    Util.encrypt(Util.base64decrypt(snapshot.child("snapshot").child("project").getValue(String.class)));
                
                String listing_path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/.sketchware/mysc/list/";
                String data_path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/.sketchware/data/";

                // Temporary
                int local_projects_length = Integer.parseInt(Util.listDir(listing_path).get(Util.listDir(listing_path).size() - 1));
                // Get available folder
                int available_id = local_projects_length + 1;
                listing_path += String.valueOf(available_id) + "/";
                data_path += String.valueOf(available_id) + "/";

                try {
                    writeToFile(listing_path, "project", project);

                    writeToFile(data_path, "file", file);
                    writeToFile(data_path, "view", view);
                    writeToFile(data_path, "resource", resource);
                    writeToFile(data_path, "logic", logic);
                    writeToFile(data_path, "library", library);
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "ERROR WHILE CLONING: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private void writeToFile(String path, String filename, byte[] data) throws IOException {
        File file = new File(path, filename);
        if (!file.exists()) {
            boolean newFile = file.createNewFile();
            if (!newFile) {
                // Error
                throw new IOException("Cannot create file");
            }
        }
        FileOutputStream stream = new FileOutputStream(file);
        try {
            stream.write(data);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                stream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}