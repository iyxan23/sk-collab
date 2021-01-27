package com.iyxan23.sketch.collab.online;

import android.os.Bundle;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.iyxan23.sketch.collab.R;
import com.iyxan23.sketch.collab.databinding.ActivityViewOnlineProjectBinding;

import org.jetbrains.annotations.NotNull;

public class ViewOnlineProjectActivity extends AppCompatActivity {

    private ActivityViewOnlineProjectBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityViewOnlineProjectBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        FloatingActionButton fab = binding.fab;
        fab.setOnClickListener(view -> Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show());

        String project_key = getIntent().getStringExtra("project_key");

        // Fetch the project from the database
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        DocumentReference project = firestore.collection("projects").document(project_key);

        project
                .get()
                .addOnCompleteListener(task -> {
                    
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}