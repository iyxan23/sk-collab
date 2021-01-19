package com.iyxan23.sketch.collab;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Source;

public class CheckActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check);

        // Initialize the Firebase Firestore
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        DocumentReference ref = firestore.collection("status").document("status");

        // Get views
        ProgressBar progressbar = findViewById(R.id.progressBar_check);
        TextView loading_text = findViewById(R.id.status_check);

        // Check if the server is open
        ref.get(Source.SERVER).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Success
                loading_text.setText("Information received");

                // Get the snapshot
                DocumentSnapshot snapshot = task.getResult();

                // Save it into a variable
                boolean isOpen = snapshot.getBoolean("is_open");

                // Check if it is 1 (open) or 0 (closed)
                if (isOpen) {
                    loading_text.setText("Server is open, Redirecting to Login Page");
                    Intent intent = new Intent(CheckActivity.this, LoginActivity.class);
                    startActivity(intent);

                    // End the activity so the user can't go back to this activity
                    finish();
                } else {
                    // Server is closed, get the message and display it to the user

                    // Get the reason
                    String reason = snapshot.get("reason", String.class);
                    reason = reason == null ? "Something weird happened, got a null response" : reason;

                    // Remove the progressbar
                    progressbar.setVisibility(View.INVISIBLE);

                    // Set the reason
                    loading_text.setText(reason);
                }
            } else {
                // Failed
                assert task.getException() != null;

                // Ight, get the exception and print the stacktrace
                task.getException().printStackTrace();

                // And also tell it to the user
                Toast.makeText(CheckActivity.this, "An error occured: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
