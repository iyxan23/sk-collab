package com.iyxan23.sketch.collab

import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase


class LoginActivity : AppCompatActivity() {

    // I have no idea what companions are, But this might solve the problem, i think
    companion object Constructor {
        // Getting the FirebaseAuth instance
        private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    }

    // Masks
    /* Wierldy, the decompiled java code doesn't initialize these variables, so i needed to do it
     * manually using hardcoded numbers
    private val EMAIL_EMPTY             : Int = 1 shr 0;
    private val PASSWORD_EMPTY          : Int = 1 shr 1;
    private val USERNAME_EMPTY          : Int = 1 shr 2;
    private val PASSWORD_LESS_THAN_6    : Int = 1 shr 3;
    private val EMAIL_INVALID           : Int = 1 shr 4;
    private val USERNAME_INVALID        : Int = 1 shr 5;
     */

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
            startActivity(mainActivityIntent)

            // Finish the activity so the user cannot go back
            // to this activity using the back button
            finish()
        }

        // onClicks ================================================================================
        loginText.setOnClickListener {
            isRegister = false

            // updateForm to remove the username text field
            updateForm()
        }

        registerText.setOnClickListener {
            isRegister = true

            // updateForm to display the username text field
            updateForm()
        }

        val emailEditText   : EditText = findViewById(R.id.email_login_text)
        val passwordEditText: EditText = findViewById(R.id.password_login_text)
        val usernameEditText: EditText = findViewById(R.id.username_login_text)

        usernameEditText.visibility = View.GONE

        val errorText: TextView = findViewById(R.id.errorText_login)

        // Focus on Email EditText
        emailEditText.requestFocus()

        loginButton.setOnClickListener {
            // Check if the inputs are filled
            val status: Int = checkFields()

            if (status == 0) {
                // Great, we can login / register now
                if (isRegister) {

                    // Register user's account
                    auth.createUserWithEmailAndPassword(
                            emailEditText.text.toString().trim(),
                            passwordEditText.text.toString()
                    ).addOnSuccessListener {
                        // Get the firebase database
                        val database = FirebaseDatabase.getInstance()
                        val usersRef = database.getReference("/userdata/")

                        // Build the user's data
                        val userdata = HashMap<String, Any>()
                        userdata["name"] = usernameEditText.text.toString()
                        userdata["uid"] = it.user!!.uid

                        // Add the user in the database
                        usersRef.setValue(userdata)

                        // Done! Redirect user to MainActivity
                        startActivity(mainActivityIntent)

                        // Finish the activity so the user cannot go back
                        // to this activity using the back button
                        finish()

                    }.addOnFailureListener {
                        // Something went wrong..
                        errorText.text = it.message
                    }
                } else {
                    // Login
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
                        errorText.text = it.message
                    }
                }
            } else {
                // Something doesn't seem right
                // PASSWORD_LESS_THAN_6
                if (status and 16 != 0)
                    // Password is less than 6 characters
                    errorText.text = "Password cannot be less than 6 characters"

                // PASSWORD_EMPTY
                if (status and 4 != 0)
                    // Password is empty
                    errorText.text = "Password cannot be empty"

                // USERNAME_INVALID
                if (status and 64 != 0 && isRegister)
                    errorText.text = "Username can only contain A-Z 0-9 -_."

                // USERNAME_EMPTY
                if (status and 8 != 0 && isRegister)
                    // Username is empty
                    errorText.text = "Username cannot be empty"

                // EMAIL_INVALID
                if (status and 32 != 0)
                    // Email is Invalid
                    errorText.text = "Email is invalid"

                // EMAIL_EMPTY
                if (status and 2 != 0)
                    // Email is empty
                    errorText.text = "Email cannot be empty"

            }
        }
    }

    // Used to update the form, when the user clicked on "Login" or "Register", Display or Remove a
    // view
    private fun updateForm() {
        // Switching login and register texts
        val loginText: TextView = findViewById(R.id.login_text)
        val registerText: TextView = findViewById(R.id.register_text)

        // Other stuff
        val rootLogin       : ConstraintLayout = findViewById(R.id.root_login)
        val usernameEditText: EditText = findViewById(R.id.username_login_text)

        val loginButton: Button = findViewById(R.id.login_button)

        if (isRegister) {
            // Change the color and sizes of the "tabs"
            registerText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f);
            registerText.setTextColor(0xFFFFFF);

            loginText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f);
            loginText.setTextColor(0x747474);

            // Email EditText animation
            TransitionManager.beginDelayedTransition(rootLogin)
            val constraintSet = ConstraintSet()
            constraintSet.clone(rootLogin)
            constraintSet.connect(R.id.email_login, ConstraintSet.TOP, R.id.username_login, ConstraintSet.BOTTOM)
            constraintSet.applyTo(rootLogin)

            // Add / Display the username edit text, because we're registering
            usernameEditText.visibility = View.VISIBLE

            // Change the text on the login button
            loginButton.text = "Register"

        } else {
            // Change the color and sizes of the "tabs"
            loginText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f);
            loginText.setTextColor(0xFFFFFF);

            registerText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f);
            registerText.setTextColor(0x747474);

            // Email EditText animation
            TransitionManager.beginDelayedTransition(rootLogin)
            val constraintSet = ConstraintSet()
            constraintSet.clone(rootLogin)
            constraintSet.connect(R.id.email_login, ConstraintSet.TOP, R.id.textView3, ConstraintSet.BOTTOM)
            constraintSet.applyTo(rootLogin)

            // Remove the username edit text, because we're logging in
            usernameEditText.visibility = View.GONE

            // Change the text on the login button
            loginButton.text = "Login"
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

        // EMAIL_EMPTY
        if (email == "")
            out += 2

        // PASSWORD_EMPTY
        if (password == "")
            out += 4

        // USERNAME_EMPTY
        if (username == "" && !isRegister)
            out += 8

        // PASSWORD_LESS_THAN_6
        if (password.length < 6)
            out += 16

        // EMAIL_INVALID
        if (!email.matches(emailRegex))
            out += 32

        // USERNAME_INVALID
        if (!username.matches(usernameRegex))
            out += 64

        return out;
    }
}