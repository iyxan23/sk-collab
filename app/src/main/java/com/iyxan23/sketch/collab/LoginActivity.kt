package com.iyxan23.sketch.collab

import android.animation.ObjectAnimator
import android.content.Intent
import android.icu.number.NumberFormatter
import android.opengl.Visibility
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.util.Property
import android.util.TypedValue
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    // Getting the FirebaseAuth instance
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    // Masks
    private val EMAIL_EMPTY             : Int = 1 shr 0;
    private val PASSWORD_EMPTY          : Int = 1 shr 1;
    private val USERNAME_EMPTY          : Int = 1 shr 2;
    private val PASSWORD_LESS_THAN_6    : Int = 1 shr 3;
    private val EMAIL_INVALID           : Int = 1 shr 4;
    private val USERNAME_INVALID        : Int = 1 shr 5;

    private var isRegister: Boolean = false;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Used to send user to MainActivity
        val mainActivityIntent = Intent(this, MainActivity::class.java)

        // The Login Button
        val loginButton: Button = findViewById(R.id.login_button)

        // Switching login and register texts
        val loginText: TextView = findViewById(R.id.login_text)
        val registerText: TextView = findViewById(R.id.register_text)

        // Check if user has logged in or not
        if (auth.currentUser != null) {
            // User has logged in!
            // Send him to MainActivity
            startActivity(mainActivityIntent);

            // Finish the activity so the user cannot go back
            // to this activity using the back button
            finishActivity(0);
        }

        // onClicks ================================================================================
        loginText.setOnClickListener {
            isRegister = false;

            // updateForm to remove the username text field
            updateForm()
        }

        registerText.setOnClickListener {
            isRegister = true;

            // updateForm to display the username text field
            updateForm()
        }

        val emailEditText   : EditText = findViewById(R.id.email_login_text)
        val passwordEditText: EditText = findViewById(R.id.password_login_text)
        val usernameEditText: EditText = findViewById(R.id.username_login_text)

        loginButton.setOnClickListener {
            // Check if the inputs are filled
            val status: Int = checkFields()

            if (status == 0) {
                // Great, we can login / register now
                auth.signInWithEmailAndPassword(
                        emailEditText.text.toString(),
                        passwordEditText.text.toString()
                ).addOnSuccessListener {
                    // Success! redirect user to the MainActivity
                    startActivity(mainActivityIntent);
                    finishActivity(0);

                }.addOnFailureListener {
                    // Something went wrong!
                    // Show a snackbar
                    Snackbar.make(findViewById(R.id.root_login), "Something went wrong!", Snackbar.LENGTH_LONG)
                            .setBackgroundTint(resources.getColor(R.color.colorAccent))  // Red color
                            .show()
                }

            } else {
                // Something doesn't seem right
                if (status and PASSWORD_LESS_THAN_6 != 0)
                    // Password is less than 6 characters
                    emailEditText.setError("Password cannot be less than 6 characters")

                if (status and PASSWORD_EMPTY != 0)
                    // Password is empty
                    passwordEditText.setError("Password cannot be empty")

                if (status and USERNAME_INVALID != 0 && isRegister)
                    usernameEditText.setError("Username can only contain A-Z 0-9 -_.")

                if (status and USERNAME_EMPTY != 0 && isRegister)
                    // Username is empty
                    usernameEditText.setError("Username cannot be empty")

                if (status and EMAIL_INVALID != 0)
                    // Email is Invalid
                    emailEditText.setError("Email is invalid")

                if (status and EMAIL_EMPTY != 0)
                    // Email is empty
                    emailEditText.setError("Email cannot be empty")

            }
        }
    }

    // Used to update the form, when the user clicked on "Login" or "Register", Display or Remove a
    // view
    private fun updateForm() {
        // Switching login and register texts
        val loginText: TextView = findViewById(R.id.login_text)
        val registerText: TextView = findViewById(R.id.register_text)

        val usernameEditText: EditText = findViewById(R.id.username_login_text)

        if (isRegister) {
            usernameEditText.visibility = View.VISIBLE

            registerText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f);
            registerText.setTextColor(0xFFFFFF);

            loginText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f);
            loginText.setTextColor(0x747474);
        } else {
            usernameEditText.visibility = View.GONE

            loginText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f);
            loginText.setTextColor(0xFFFFFF);

            registerText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f);
            registerText.setTextColor(0x747474);
        }
    }

    // Used to check fields, like if email is empty, password empty, etc.
    private fun checkFields(): Int {
        val email: String = findViewById<EditText>(R.id.email_login_text).text.toString().trim()
        val password: String = findViewById<EditText>(R.id.password_login_text).text.toString()
        val username: String = findViewById<EditText>(R.id.username_login_text).text.toString().trim()

        val emailRegex = Regex("[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+")
        val usernameRegex = Regex("[a-zA-Z0-9._-]+")

        var out = 0

        if (email == "")
            out += EMAIL_EMPTY

        if (password == "")
            out += PASSWORD_EMPTY

        if (username == "" && !isRegister)
            out += USERNAME_EMPTY

        if (password.length < 6)
            out += PASSWORD_LESS_THAN_6

        if (!email.matches(emailRegex))
            out += EMAIL_INVALID

        if (!username.matches(usernameRegex))
            out += USERNAME_INVALID

        return out;
    }
}