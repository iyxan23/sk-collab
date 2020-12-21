package com.iyxan23.sketch.collab

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.database.*

class CheckActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_check)

        // Initialize the Firebase Database
        val database: FirebaseDatabase = FirebaseDatabase.getInstance()
        val ref: DatabaseReference = database.getReference("/status");

        // Views
        val progressbar: ProgressBar = findViewById(R.id.progressBar_check)
        val loading_text: TextView = findViewById(R.id.status_check);

        // Check if the server is open
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                // Save it into a variable
                val isOpen : Int? = snapshot.child("open").getValue(Int::class.java)

                // Check if it is 1 (open) or 0 (closed)
                if (isOpen == 1) {
                    val intent = Intent(this@CheckActivity, LoginActivity::class.java)
                    startActivity(intent);
                } else {
                    // Server is closed, get the message and display it to the user

                    // Get the reason
                    val reason : String? = snapshot.child("reason").getValue(String::class.java)

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