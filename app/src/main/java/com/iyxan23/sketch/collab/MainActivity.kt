package com.iyxan23.sketch.collab

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import org.json.JSONObject
import java.io.File

class MainActivity : AppCompatActivity() {

    // project keys
    // Pair<project_key, "project" file>
    val publicProjectsOwned = ArrayList<Pair<String, JSONObject>>()
    val userProjects = ArrayList<Pair<String, JSONObject>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Check if Read and Write external storage permission is granted
        // why scoped storage? :(
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED ||
            ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {

            // Tell the user first of why you need to grant the permission
            Toast.makeText(this, "We need storage permission to access your sketchware projects. SketchCollab can misbehave if you denied the permission.", Toast.LENGTH_LONG).show()
            // Reuest the permission(s)
            ActivityCompat.requestPermissions(this,
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE),
            100)
        }

        val internalPath: String = filesDir.absolutePath
        val lastChanges = File("$internalPath/last_changes")

        // Get user projects, and projects that the user collaborates
        val database: FirebaseDatabase = FirebaseDatabase.getInstance()
        val auth: FirebaseAuth = FirebaseAuth.getInstance()
        val userProjectsRef: DatabaseReference = database.getReference("/userprojects/" + auth.uid)
        val projectsRef: DatabaseReference = database.getReference("/projects/" + auth.uid)

        projectsRef.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (project in snapshot.children) {
                    if (project.child("author").value == auth.uid) {
                        val projectBase64: String = project.child("snapshot").child("project").value.toString()
                        val json = JSONObject(Util.base64decode(projectBase64))

                        // Because project id can vary on different devices
                        json.optInt("sc_id")

                        val pair = Pair(project.key!!, json)
                        publicProjectsOwned.add(pair)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity, "Error while fetching data: " + error.message, Toast.LENGTH_LONG).show()
            }

        })

        userProjectsRef.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (project in snapshot.children) {
                    val projectBase64: String = project.child("snapshot").child("project").value.toString()
                    val json = JSONObject(Util.base64decode(projectBase64))

                    // Because project id can vary on different devices
                    json.opt("sc_id")

                    val pair = Pair(project.key!!, json)
                    userProjects.add(pair)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity, "Error while fetching data: " + error.message, Toast.LENGTH_LONG).show()
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            1 -> {
                if (resultCode == PackageManager.PERMISSION_GRANTED) {
                    // TODO: Fetch sketchware projects
                }
            }
        }
    }
}