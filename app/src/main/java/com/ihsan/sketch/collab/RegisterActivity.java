package com.ihsan.sketch.collab;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import android.animation.Animator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class RegisterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        getSupportActionBar().hide();
    }

    @Override
    protected void onStart() {
        super.onStart();

        final Button lanjut = findViewById(R.id.lanjut);
        final ProgressBar lanjut_p = findViewById(R.id.loadingLanjut);
        final Intent i = getIntent();
        final TextInputLayout input = findViewById(R.id.name);
        final String name = input.getEditText().getText().toString().trim();
        lanjut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (name.equals("")) {
                    input.setError(getString(R.string.name_empty));
                } else if (!isAsciiPrintable(name)) {
                    input.setError(getString(R.string.name_not_ascii));
                } else {
                lanjut_p.setVisibility(View.VISIBLE);
                final FirebaseAuth auth = FirebaseAuth.getInstance();
                auth.createUserWithEmailAndPassword(i.getStringExtra("email"), i.getStringExtra("password"))
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                lanjut_p.setVisibility(View.INVISIBLE);
                                Snackbar.make(findViewById(R.id.parent_register), "ERROR: " + e.toString(), Snackbar.LENGTH_LONG).setBackgroundTint(Color.RED).show();
                            }
                        }).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        lanjut_p.setVisibility(View.INVISIBLE);
                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                        DatabaseReference userdata = database.getReference("userdata");
                        userdata.child(authResult.getUser().getUid())
                                .child("uid")
                                .setValue(authResult.getUser().getUid());
                        userdata.child(authResult.getUser().getUid())
                                .child("name")
                                .setValue(name).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Intent i = new Intent(RegisterActivity.this, SplashActivity.class);
                                        startActivity(i);
                                        finish();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(RegisterActivity.this, "WARNING: ERROR DURING SAVING YOUR NAME TO THE DATABASE, YOU MUST EDIT IT YOURSELF ON THE HOME PAGE", Toast.LENGTH_LONG)
                                                .show();
                                        Intent i = new Intent(RegisterActivity.this, SplashActivity.class);
                                        startActivity(i);
                                        finish();
                                    }
                                });
                        }
                    });
                }
            }
        });
    }

    public static boolean isAsciiPrintable(String v) {
        return StandardCharsets.US_ASCII.newEncoder().canEncode(v);
    }

}