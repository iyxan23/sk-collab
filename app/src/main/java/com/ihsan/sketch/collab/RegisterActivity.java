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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

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

        final View top_reg = findViewById(R.id.part_reg);
        top_reg.post(new Runnable() {
            @Override
            public void run() {
                int cx = top_reg.getWidth() / 2;
                int cy = top_reg.getHeight() / 2;
                float finalRadius = (float) Math.hypot(cx, cy);
                Animator anim = ViewAnimationUtils.createCircularReveal(top_reg, cx, cy, 0, finalRadius);
                top_reg.setVisibility(View.VISIBLE);
                anim.setDuration(500);
                anim.start();
            }
        });

        final Button lanjut = findViewById(R.id.lanjut);
        final ProgressBar lanjut_p = findViewById(R.id.loadingLanjut);
        final Intent i = getIntent();
        lanjut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                lanjut_p.setVisibility(View.VISIBLE);
                FirebaseAuth auth = FirebaseAuth.getInstance();
                auth.createUserWithEmailAndPassword(i.getStringExtra("email"), i.getStringExtra("password"))
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            lanjut_p.setVisibility(View.INVISIBLE);
                            Snackbar.make(findViewById(R.id.parent_register), "GAGAL: " + e.toString(), Snackbar.LENGTH_LONG).setBackgroundTint(Color.RED).show();
                        }
                    }).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                        @Override
                        public void onSuccess(AuthResult authResult) {
                            lanjut_p.setVisibility(View.INVISIBLE);
                        }
                    });
            }
        });
    }
}