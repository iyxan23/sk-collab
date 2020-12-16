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

import org.json.JSONException;
import org.json.JSONObject;

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

                /*
                byte[] file =       Util.encrypt(Util.base64decrypt(snapshot.child("snapshot").child("file").getValue(String.class)));
                byte[] view =       Util.encrypt(Util.base64decrypt(snapshot.child("snapshot").child("view").getValue(String.class)));
                byte[] resource =   Util.encrypt(Util.base64decrypt(snapshot.child("snapshot").child("resource").getValue(String.class)));
                byte[] logic =      Util.encrypt(Util.base64decrypt(snapshot.child("snapshot").child("logic").getValue(String.class)));
                byte[] library =    Util.encrypt(Util.base64decrypt(snapshot.child("snapshot").child("library").getValue(String.class)));
                 */

                String file =       Util.base64decrypt(snapshot.child("snapshot").child("file").getValue(String.class));
                String view =       Util.base64decrypt(snapshot.child("snapshot").child("view").getValue(String.class));
                String resource =   Util.base64decrypt(snapshot.child("snapshot").child("resource").getValue(String.class));
                String logic =      Util.base64decrypt(snapshot.child("snapshot").child("logic").getValue(String.class));
                String library =    Util.base64decrypt(snapshot.child("snapshot").child("library").getValue(String.class));
                
                String listing_path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/.sketchware/mysc/list/";
                String data_path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/.sketchware/data/";

                // Temporary
                int local_projects_length = Integer.parseInt(Util.listDir(listing_path).get(Util.listDir(listing_path).size() - 1));
                // Get available folder
                int available_id = local_projects_length + 1;
                listing_path += available_id + "/";
                data_path += available_id + "/";

                String project_string = Util.base64decrypt(snapshot.child("snapshot").child("project").getValue(String.class));
                JSONObject project_json = null;
                try {
                    project_json = new JSONObject(project_string);
                    project_json.put("sc_id", available_id);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                /*
                writeToFile(listing_path, "project", project);

                writeToFile(data_path, "file", file);
                writeToFile(data_path, "view", view);
                writeToFile(data_path, "resource", resource);
                writeToFile(data_path, "logic", logic);
                writeToFile(data_path, "library", library);
                */

                Util.encrypt(listing_path + "project", project_json.toString());

                Util.encrypt(data_path +"file", file);
                Util.encrypt(data_path + "view", view);
                Util.encrypt(data_path + "resource", resource);
                Util.encrypt(data_path + "logic", logic);
                Util.encrypt(data_path + "library", library);

                Toast.makeText(getApplicationContext(), "Clone Successful", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CloneActivity.this, "Error while cloning, " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}