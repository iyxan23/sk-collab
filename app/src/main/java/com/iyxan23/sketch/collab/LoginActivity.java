package com.iyxan23.sketch.collab;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.transition.TransitionManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    private boolean isRegister = false;

    @SuppressLint("SetTextI18n")  // TODO: FIX THIS
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // The Login Button
        Button loginButton = findViewById(R.id.login_button);

        // Switching login and register texts
        TextView loginText = findViewById(R.id.login_text);
        TextView registerText = findViewById(R.id.register_text);

        // Check if user has logged in or not
        if (auth.getCurrentUser() != null) {
            // User has logged in!
            // Send him to MainActivity
            startActivity(new Intent(this, MainActivity.class));

            // Finish the activity so the user cannot go back
            // to this activity using the back button
            finish();
        }

        // onClicks ================================================================================
        loginText.setOnClickListener(v -> {
            isRegister = false;

            // updateForm to remove the username text field
            updateForm();
        });

        registerText.setOnClickListener(v -> {
            isRegister = true;

            // updateForm to display the username text field
            updateForm();
        });

        EditText emailEditText    = findViewById(R.id.email_login_text);
        EditText passwordEditText = findViewById(R.id.password_login_text);
        EditText usernameEditText = findViewById(R.id.username_login_text);

        usernameEditText.setVisibility(View.GONE);

        TextView errorText = findViewById(R.id.errorText_login);

        // Focus on Email EditText
        emailEditText.requestFocus();

        loginButton.setOnClickListener(v -> {
            // Show the progressbar
            findViewById(R.id.login_progressbar).setVisibility(View.VISIBLE);

            // Check if the inputs are filled
            String res = checkFields();

            if (res == null) {
                // Great, we can login / register now
                if (isRegister) {

                    // Register user's account
                    auth.createUserWithEmailAndPassword(
                            emailEditText.getText().toString().trim(),
                            passwordEditText.getText().toString()
                    ).addOnSuccessListener(s -> {
                        // Get the firebase firestore
                        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
                        CollectionReference usersRef = firestore.collection("userdata");

                        // Get the user
                        FirebaseUser user = s.getUser();
                        if (user == null) {
                            errorText.setText("User is null");
                            return;
                        }

                        // Build the user's data
                        HashMap<String, Object> userdata = new HashMap<>();
                        userdata.put("name", usernameEditText.getText().toString());
                        userdata.put("uid", user.getUid());

                        // Add the user in the database
                        usersRef.add(userdata).addOnCompleteListener(task -> {
                            // Hide the progressbar
                            findViewById(R.id.login_progressbar).setVisibility(View.GONE);

                            if (task.isSuccessful()) {
                                // Done! Redirect user to MainActivity
                                startActivity(new Intent(this, MainActivity.class));

                                // Finish the activity so the user cannot go back
                                // to this activity using the back button
                                finish();
                            } else {
                                // Hide the progressbar
                                findViewById(R.id.login_progressbar).setVisibility(View.GONE);

                                // Something went wrong..
                                errorText.setText("Error: " + Objects.requireNonNull( task.getException() ).getMessage());
                            }
                        });
                    }).addOnFailureListener(f -> {
                        // Hide the progressbar
                        findViewById(R.id.login_progressbar).setVisibility(View.GONE);

                        // Something went wrong..
                        errorText.setText(f.getMessage());
                    });
                } else {
                    // Login
                    auth.signInWithEmailAndPassword(
                            emailEditText.getText().toString(),
                            passwordEditText.getText().toString()
                    ).addOnSuccessListener(s -> {
                        // Success! redirect user to the MainActivity
                        startActivity(new Intent(this, MainActivity.class));
                        finish();

                    }).addOnFailureListener(f -> {
                        // Hide the progressbar
                        findViewById(R.id.login_progressbar).setVisibility(View.GONE);

                        // Something went wrong!
                        errorText.setText(f.getMessage());
                    });
                }
            } else {
                // Hide the progressbar
                findViewById(R.id.login_progressbar).setVisibility(View.GONE);

                // Something doesn't seem right
                errorText.setText(res);
            }
        });
    }

    // Used to update the form, when the user clicked on "Login" or "Register", Display or Remove a
    // view
    @SuppressLint("SetTextI18n")  // TODO: FIX THIS
    private void updateForm() {
        // Switching login and register texts
        TextView loginText = findViewById(R.id.login_text);
        TextView registerText = findViewById(R.id.register_text);

        // Other stuff
        ConstraintLayout rootLogin = findViewById(R.id.root_login);
        EditText usernameEditText = findViewById(R.id.username_login_text);

        Button loginButton = findViewById(R.id.login_button);

        if (isRegister) {
            // Change the color and sizes of the "tabs"
            loginText.setScaleX(1f);
            loginText.setScaleY(1f);
            registerText.setTextColor(0xFFFFFF);

            loginText.setScaleX(0.8f);
            loginText.setScaleY(0.8f);
            loginText.setTextColor(0x747474);

            // Email EditText animation
            TransitionManager.beginDelayedTransition(rootLogin);
            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(rootLogin);
            constraintSet.connect(R.id.email_login, ConstraintSet.TOP, R.id.username_login, ConstraintSet.BOTTOM);
            constraintSet.applyTo(rootLogin);

            // Add / Display the username edit text, because we're registering
            usernameEditText.setVisibility(View.VISIBLE);

            // Change the text on the login button
            loginButton.setText("Register");

        } else {
            // Change the color and sizes of the "tabs"
            loginText.setScaleX(1f);
            loginText.setScaleY(1f);
            loginText.setTextColor(0xFFFFFF);

            registerText.setScaleX(0.8f);
            registerText.setScaleY(0.8f);
            registerText.setTextColor(0x747474);

            // Email EditText animation
            TransitionManager.beginDelayedTransition(rootLogin);
            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(rootLogin);
            constraintSet.connect(R.id.email_login, ConstraintSet.TOP, R.id.textView3, ConstraintSet.BOTTOM);
            constraintSet.applyTo(rootLogin);

            // Remove the username edit text, because we're logging in
            usernameEditText.setVisibility(View.GONE);

            // Change the text on the login button
            loginButton.setText("Login");
        }
    }

    // Used to check fields, like if email is empty, password empty, etc.
    private String checkFields() {
        String email = ((EditText) findViewById(R.id.email_login_text)).getText().toString().trim();
        String password = ((EditText) findViewById(R.id.password_login_text)).getText().toString();
        String username = ((EditText) findViewById(R.id.username_login_text)).getText().toString().trim();

        String res = null;

        String emailRegex = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
        String usernameRegex = "[a-zA-Z0-9._-]+";

        // EMAIL_EMPTY
        if (email.equals(""))
            res = "Email cannot be empty";

        // PASSWORD_EMPTY
        if (password.equals(""))
            res = "Password cannot be empty";

        // USERNAME_INVALID
        if (!username.matches(usernameRegex) && isRegister)
            res = "Username can only contain A-Z a-z 0-9 -_.";

        // USERNAME_EMPTY
        if (username.equals("") && isRegister)
            res = "Username cannot be empty";

        // PASSWORD_LESS_THAN_6
        if (password.length() < 6)
            res = "Password cannot be less than 6 characters";

        // EMAIL_INVALID
        if (!email.matches(emailRegex))
            res = "Email is invalid";

        return res;
    }
}
