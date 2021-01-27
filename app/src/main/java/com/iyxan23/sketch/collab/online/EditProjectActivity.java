package com.iyxan23.sketch.collab.online;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.iyxan23.sketch.collab.R;

public class EditProjectActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_project);

        String description_before = getIntent().getStringExtra("description");

        TextInputEditText description_edit = findViewById(R.id.description_edit_project_text);
        description_edit.setText(description_before);
    }

    public void save(View view) {
        TextInputEditText description_edit = findViewById(R.id.description_edit_project_text);

        description_edit.setEnabled(false);
        findViewById(R.id.progressbar_edit_project).setVisibility(View.VISIBLE);

        String project_key = getIntent().getStringExtra("project_key");

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        DocumentReference project_reference = firestore.collection("projects").document(project_key);

        project_reference
                .update("description", description_edit.getText().toString())
                .addOnCompleteListener(task -> {
                    findViewById(R.id.progressbar_edit_project).setVisibility(View.GONE);

                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Updated, refresh the page to see the change", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Error while updating: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }

                    finish();
                });
    }
}