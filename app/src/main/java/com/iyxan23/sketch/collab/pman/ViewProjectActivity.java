package com.iyxan23.sketch.collab.pman;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.ImageViewCompat;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.iyxan23.sketch.collab.R;
import com.iyxan23.sketch.collab.Util;
import com.iyxan23.sketch.collab.models.SketchwareProject;
import com.iyxan23.sketch.collab.online.UploadActivity;
import com.iyxan23.sketch.collab.online.ViewOnlineProjectActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class ViewProjectActivity extends AppCompatActivity {

    SketchwareProject project;
    boolean is_sketchcollab_project;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_project);

        Intent intent = getIntent();

        // Get the project extra
        project = intent.getParcelableExtra("project");

        if (project.metadata == null) {
            finish_corrupted();
        }

        // Is this a SketchCollab Project?
        try {
            is_sketchcollab_project = project.isSketchCollabProject();
        } catch (JSONException e) {
            finish_corrupted();
        }

        if (!is_sketchcollab_project) {
            // Change the UI if this isn't a SketchCollab project,
            // No need to do the opposite, the opposite is already in the UI itself
            findViewById(R.id.sketchcollab_project_indicator).setVisibility(View.GONE);

            ((TextView) findViewById(R.id.textView24)).setTextColor(0xFF555555);
            ImageViewCompat.setImageTintList(findViewById(R.id.imageView15), ColorStateList.valueOf(0xFF555555));

            ((TextView) findViewById(R.id.textView25)).setTextColor(0xFF555555);
            ImageViewCompat.setImageTintList(findViewById(R.id.imageView16), ColorStateList.valueOf(0xFF555555));

            ((TextView) findViewById(R.id.textView26)).setTextColor(0xFFFFFFFF);
            ImageViewCompat.setImageTintList(findViewById(R.id.imageView17), ColorStateList.valueOf(0xFFFFFFFF));
        }
    }

    private void finish_corrupted() {
        Toast.makeText(this, "This project is corrupted", Toast.LENGTH_SHORT).show();

        finish();
    }

    public void back(View view) {
        onBackPressed();
    }

    public void openOnlineProject(View view) {
        // Only execute this if this is a sketchcollab project
        if (is_sketchcollab_project) {
            Intent i = new Intent(this, ViewOnlineProjectActivity.class);

            try {
                i.putExtra("project_key", project.getProjectID());
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to get project key, this project is possibly corrupted.", Toast.LENGTH_SHORT).show();

                return;
            }

            startActivity(i);
        }
    }

    public void removeReference(View view) {
        // Only execute this if this is a sketchcollab project
        if (is_sketchcollab_project) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Confirmation");
            builder.setMessage("This will remove the reference of your sketchcollab project, diconnecting the project from the server. You can though, clone the project to get a project with the reference to it, but this is permanent for this project");
            builder.setPositiveButton("Yes", (dialog, which) -> {
                // Initiate the Intercontential Ballistic Missile
                // jk

                try {
                    // Remove the references / keys
                    JSONObject project_json = new JSONObject(Util.decrypt(project.mysc_project));
                    project_json.remove("sk-collab-key");
                    project_json.remove("sk-collab-owner");
                    project_json.remove("sk-collab-latest-commit");
                    project_json.remove("sk-collab-project-visibility");

                    // then put it back
                    project.mysc_project = Util.encrypt(project_json.toString().getBytes());

                    // and then apply it
                    project.applyChanges();

                    // Ok done, time to refresh the activity
                    recreate();

                    // Oh ye, just in case, close the dialog
                    dialog.dismiss();

                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "An error occured: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });

            builder.setNegativeButton("Cancel", (dialog, which) -> {
                // ight ima head out
                dialog.dismiss();
            });

            // Build and Show the dialog!
            builder.create().show();
        }
    }

    public void uploadProject(View view) {
        // Only execute if this isn't a sketchcollab project
        if (!is_sketchcollab_project) {
            // Open up UploadActivity
            Intent i = new Intent(this, UploadActivity.class);

            i.putExtra("project_id", project.metadata.id);
            // Note: project.metadata will never be null because we already checked it in onCreate

            // Alright, let's fire it up!
            startActivity(i);
        }
    }
}