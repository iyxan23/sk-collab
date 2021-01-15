package com.iyxan23.sketch.collab;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

public class CheckActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check);

        // Initialize the Firebase Database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("/status");

        // Get views
        ProgressBar progressbar = findViewById(R.id.progressBar_check);
        TextView loading_text = findViewById(R.id.status_check);

        // Check if the server is open
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("SetTextI18n")  // TODO: FIX THIS
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                loading_text.setText("Information recieved");

                // Save it into a variable
                Integer isOpen = snapshot.child("open").getValue(int.class);

                // Nullcheck
                isOpen = isOpen == null ? 0 : isOpen;

                // Check if it is 1 (open) or 0 (closed)
                if (isOpen == 1) {
                    loading_text.setText("Server is open, Redirecting to Login Page");
                    Intent intent = new Intent(CheckActivity.this, LoginActivity.class);
                    startActivity(intent);

                    // End the activity so the user can't go back to this activity
                    finish();
                } else {
                    // Server is closed, get the message and display it to the user

                    // Get the reason
                    String reason = snapshot.child("reason").getValue(String.class);
                    reason = reason == null ? "Something weird happened, got a null response" : reason;

                    // Remove the progressbar
                    progressbar.setVisibility(View.INVISIBLE);

                    // Set the reason
                    loading_text.setText(reason);
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                Toast.makeText(CheckActivity.this, "An error occured: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
