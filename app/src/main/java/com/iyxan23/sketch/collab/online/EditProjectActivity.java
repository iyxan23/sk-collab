package com.iyxan23.sketch.collab.online;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.iyxan23.sketch.collab.R;
import com.iyxan23.sketch.collab.models.Userdata;
import com.iyxan23.sketch.collab.pickers.UserPicker;

import java.util.ArrayList;
import java.util.Objects;

public class EditProjectActivity extends AppCompatActivity {

    ArrayList<Userdata> members = new ArrayList<>();
    ArrayList<Userdata> members_before = new ArrayList<>();
    String description_before;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_project);

        description_before = getIntent().getStringExtra("description");
        members = getIntent().getParcelableArrayListExtra("members");
        members_before.addAll(members);

        TextInputEditText description_edit = findViewById(R.id.description_edit_project_text);
        description_edit.setText(description_before);
    }

    public void save(View view) {
        TextInputEditText description_edit = findViewById(R.id.description_edit_project_text);

        description_edit.setEnabled(false);
        view.setEnabled(false);

        findViewById(R.id.progressbar_edit_project).setVisibility(View.VISIBLE);

        String project_key = getIntent().getStringExtra("project_key");

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        DocumentReference project_reference = firestore.collection("projects").document(project_key);

        WriteBatch writeBatch = firestore.batch();

        // Check if the description has edited or not
        if (!description_edit.getText().toString().equals(description_before)) {
            // Nop it's changed, update it
            writeBatch.update(project_reference, "description", description_edit.getText().toString());
        }

        // Check if the members list is updated or not
        if (!members_before.equals(members)) {
            // Yep it has been edited, update it

            // Put all of those members into one String arraylist
            ArrayList<String> members_ = new ArrayList<>();

            for (int i = 0; i < members.size(); i++) {
                members_.add(members.get(i).getUid());
            }

            writeBatch.update(project_reference, "members", members_);
        }

        // Commit the writeBatch!
        writeBatch
                .commit()
                .addOnSuccessListener(task -> {
                    findViewById(R.id.progressbar_edit_project).setVisibility(View.GONE);

                    Toast.makeText(this, "Updated, refresh the page to see the change", Toast.LENGTH_SHORT).show();

                    finish();
                })
                .addOnFailureListener(e -> {
                    findViewById(R.id.progressbar_edit_project).setVisibility(View.GONE);

                    Toast.makeText(this, "Error while updating: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    final int MEMBER_USER_PICK_REQ_CODE = 20;

    // When the EDIT button is clicked
    public void edit_member_click(View view) {
        // Open UserPickActivity
        Intent pick_user_intent = new Intent(this, UserPicker.class);
        pick_user_intent.putExtra("initial_data", members);
        startActivityForResult(pick_user_intent, MEMBER_USER_PICK_REQ_CODE);
    }

    private void update_members_text() {
        TextView members_list = findViewById(R.id.members_list);

        if (members.size() == 0) {
            members_list.setText("None");
            return;
        }

        StringBuilder result = new StringBuilder();

        int index = 0;
        for (Userdata userdata: members) {
            result.append(index == 0 ? "" : ", ").append(userdata.getName());
            index++;
        }

        members_list.setText(result);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == MEMBER_USER_PICK_REQ_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                members.addAll( Objects.requireNonNull(data) .getParcelableArrayListExtra("selected_users"));

                update_members_text();
            }
        }
    }
}