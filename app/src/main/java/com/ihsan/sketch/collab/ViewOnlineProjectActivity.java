package com.ihsan.sketch.collab;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.transition.Explode;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

public class ViewOnlineProjectActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);
        getWindow().setEnterTransition(new Explode());
        getWindow().setExitTransition(new Explode());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_online_project);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setElevation(0);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent i = getIntent();
        String project_id = i.getStringExtra("project_id");

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("/projects/" + project_id);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    TextView appname = findViewById(R.id.view_appname);
                    final TextView author  = findViewById(R.id.view_author);
                    TextView desc = findViewById(R.id.view_desc);
                    TextView source = findViewById(R.id.view_source);

                    appname.setText(snapshot.child("name").getValue(String.class));
                    desc.setText(snapshot.child("desc").getValue(String.class));
                    source.setText(snapshot.child("open").getValue(Boolean.class) ? "Open-sourced" : "Not Open-sourced");

                    database.getReference("userdata/" + snapshot.child("author").getValue(String.class) + "/name")
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()) {
                                        author.setText(snapshot.getValue(String.class));
                                    } else {
                                        Toast.makeText(ViewOnlineProjectActivity.this, "Error: Author does not exist", Toast.LENGTH_LONG)
                                                .show();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Toast.makeText(ViewOnlineProjectActivity.this, "Error: Cannot get author's name", Toast.LENGTH_LONG)
                                            .show();
                                }
                            });

                } else {
                    Toast.makeText(ViewOnlineProjectActivity.this, "Error: Project doesn't exist", Toast.LENGTH_LONG)
                    .show();
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}