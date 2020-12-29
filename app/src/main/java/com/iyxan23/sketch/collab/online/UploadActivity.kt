package com.iyxan23.sketch.collab.online

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.iyxan23.sketch.collab.R
import com.iyxan23.sketch.collab.Util
import com.iyxan23.sketch.collab.models.SketchwareProject
import java.util.*

class UploadActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload)

        val projectId: Int = intent.getIntExtra("projectid", -1)

        if (projectId == -1) {
            // No extras given
            // Exit instead
            finish()
            return
        }

        // Get the sketchware project
        val swProj: SketchwareProject? = Util.getSketchwareProject(projectId)

        if (swProj == null) {
            // Project doesn't exist
            // Exit instead
            finish()
            return
        }

        // Set the TextView
        val projectName: TextView = findViewById(R.id.project_name_upload)
        projectName.text = swProj.metadata.projectName

        val uploadButton: Button = findViewById(R.id.upload_upload)
        val isPrivate: SwitchMaterial = findViewById(R.id.private_upload)
        val isOpenSource: SwitchMaterial = findViewById(R.id.open_source_upload)

        val description: EditText = findViewById(R.id.description_upload)
        val name: EditText = findViewById(R.id.name_upload)

        val database: FirebaseDatabase = FirebaseDatabase.getInstance()
        val auth: FirebaseAuth = FirebaseAuth.getInstance()

        uploadButton.setOnClickListener {
            val projectReference: DatabaseReference = database.getReference("/" +
                    if (isPrivate.isChecked)
                        "userdata/" + auth.uid + "/projects"

                    else
                        "projects"
            )
            val pushKey: String? = projectReference.push().key

            val data: HashMap<String, Any> = hashMapOf(
                    "name" to name.text,
                    "description" to description.text,
                    "author" to auth.uid!!,
                    "version" to 1,
                    "open" to isOpenSource.isChecked,
                    "snapshot" to hashMapOf(
                            "commit_id" to "initial",   // Non-existent commit in the database, used to indicate the initial commit
                            "file" to Util.base64encode(swProj.file),
                            "library" to Util.base64encode(swProj.library),
                            "logic" to Util.base64encode(swProj.logic),
                            "project" to Util.base64encode(swProj.project),
                            "resource" to Util.base64encode(swProj.resource),
                            "view" to Util.base64encode(swProj.view)
                    )
            )

            val progressDialog = ProgressDialog(this@UploadActivity)
            progressDialog.setTitle("Uploading project")
            progressDialog.setMessage("Uploading " + name.text + ", please wait")
            progressDialog.show()
            progressDialog.isIndeterminate = true

            projectReference.child(pushKey!!)
                    .setValue(data)

            .addOnSuccessListener {
                progressDialog.dismiss()

                Toast.makeText(this@UploadActivity, "Project Uploaded", Toast.LENGTH_LONG).show()
                finish()

            }.addOnFailureListener {
                progressDialog.dismiss()
                Toast.makeText(this@UploadActivity, "An error occured: " + it.message, Toast.LENGTH_LONG).show()
            }
        }
    }
}