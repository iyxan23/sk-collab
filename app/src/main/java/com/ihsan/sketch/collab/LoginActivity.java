package com.ihsan.sketch.collab;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;

import android.content.Intent;
import android.os.Bundle;
import android.transition.Explode;
import android.transition.Slide;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);
        getWindow().setEnterTransition(new Slide());
        getWindow().setExitTransition(new Explode());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        getSupportActionBar().hide();

        auth = FirebaseAuth.getInstance();

        final TextInputLayout email = findViewById(R.id.email);
        final TextInputLayout passw = findViewById(R.id.passw);
        Button login = findViewById(R.id.login);
        final Button register = findViewById(R.id.register);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText email_ = email.getEditText();
                EditText passw_ = passw.getEditText();
                String email_text = email_.getText().toString().trim();
                String password = passw_.getText().toString();
                if (email_text.equals("")) {
                    email.setError(getString(R.string.email_empty));
                } else if (!isEmailValid(email_text)) {
                    email.setError(getString(R.string.email_not_valid));
                } else if (password.length() <= 4) {
                    passw.setError(getString(R.string.pwd_length_less_5));
                } else {
                    auth.signInWithEmailAndPassword(email_text, password)
                            .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                                @Override
                                public void onSuccess(AuthResult authResult) {
                                    Intent i = new Intent(LoginActivity.this, HomeActivity.class);
                                    startActivity(i);
                                    finish();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(getApplicationContext(), "ERROR: " + e.toString(), Toast.LENGTH_SHORT).show();
                                }
                            }
                    );
                }
            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText email_ = email.getEditText();
                EditText passw_ = passw.getEditText();
                String email_text = email_.getText().toString().trim();
                String password = passw_.getText().toString();
                if (email_text.equals("")) {
                    email.setError(getString(R.string.email_empty));
                } else if (!isEmailValid(email_text)) {
                    email.setError(getString(R.string.email_not_valid));
                } else if (password.length() <= 4) {
                    passw.setError(getString(R.string.pwd_length_less_5));
                } else {
                    Intent i = new Intent(LoginActivity.this, RegisterActivity.class);
                    ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(LoginActivity.this, findViewById(R.id.top_login), "top_login_reg");
                    i.putExtra("email", email_text);
                    i.putExtra("password", password);
                    startActivity(i, optionsCompat.toBundle());
                }
            }
        });
    }

    boolean isEmailValid(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
}