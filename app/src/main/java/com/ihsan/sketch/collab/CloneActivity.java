package com.ihsan.sketch.collab;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class CloneActivity extends AppCompatActivity {

    String project_id;
    String project_name;
    String project_author;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clone);

        getSupportActionBar().setTitle("Clone Project");

        Intent i = getIntent();
        project_id = i.getStringExtra("id");
        project_author = i.getStringExtra("author");
        project_name = i.getStringExtra("name");

        TextView txtview = findViewById(R.id.txtview_clone2);
        txtview.setText(project_name + " by " + project_author);

        Button clone = findViewById(R.id.btn_clone_clone);
        Button cancel = findViewById(R.id.btn_cancel_clone);

        clone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cloneProject(project_id);
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void cloneProject(String project_id) {
        
    }
}