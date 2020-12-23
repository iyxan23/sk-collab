package com.iyxan23.sketch.collab

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*


class CheckActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_check)

        val logoRed     = findViewById<View>(R.id.logo_red_part_check)
        val logoGreen   = findViewById<View>(R.id.logo_green_part_check)
        val logoBranch  = findViewById<View>(R.id.logo_branch_part_check)

        // Used to determine which logo part is going to be animated
        var animationIndex = 0

        // Animate the logo coz why not lol
        val bump_animation: Animation = AnimationUtils
                .loadAnimation(this, R.anim.logo_bump_animation).apply {
            repeatCount = Animation.INFINITE
            repeatMode = Animation.REVERSE
        }

        bump_animation.setAnimationListener(object: Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {}

            override fun onAnimationEnd(animation: Animation?) {
                when (animationIndex) {
                    0 -> {
                        logoRed.startAnimation(bump_animation)
                    }
                    1 -> {
                        logoGreen.startAnimation(bump_animation)
                    }
                    2 -> {
                        logoBranch.startAnimation(bump_animation)
                    }
                }
                // Set animationIndex to be +1 if animationIndex is not 2, otherwise, set it to 0
                animationIndex = if (animationIndex != 2) animationIndex + 1 else 0
            }

            override fun onAnimationRepeat(animation: Animation?) {}
        })

        // Initialize the Firebase Database
        val database: FirebaseDatabase = FirebaseDatabase.getInstance()
        val ref: DatabaseReference = database.getReference("/status");

        // Views
        val progressbar: ProgressBar = findViewById(R.id.progressBar_check)
        val loading_text: TextView = findViewById(R.id.status_check);

        // Check if the server is open
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                loading_text.text = "Information recieved"

                // Save it into a variable
                val isOpen: Int? = snapshot.child("open").getValue(Int::class.java)

                // Check if it is 1 (open) or 0 (closed)
                if (isOpen == 1) {
                    loading_text.text = "Server is open, Redirecting to Login Page"
                    val intent = Intent(this@CheckActivity, LoginActivity::class.java)
                    startActivity(intent);
                } else {
                    // Server is closed, get the message and display it to the user

                    // Get the reason
                    val reason: String? = snapshot.child("reason").getValue(String::class.java)

                    // Remove the progressbar
                    progressbar.visibility = View.INVISIBLE

                    // Set the reason
                    loading_text.text = reason
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Hmm something went wrong
                Toast.makeText(this@CheckActivity, "An error occured: " + error.message, Toast.LENGTH_LONG).show()
            }
        })
    }
}