package com.ihsan.sketch.collab;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Bundle;
import android.transition.Explode;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

public class ViewOnlineProjectActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);
        getWindow().setEnterTransition(new Explode());
        getWindow().setExitTransition(new Explode());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_online_project);

        getSupportActionBar().setTitle("View Project");
        getSupportActionBar().setElevation(0);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent i = getIntent();
        String project_id = i.getStringExtra("key");

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("/projects/" + project_id);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    TextView appname = findViewById(R.id.view_appname);
                    final TextView author  = findViewById(R.id.view_author);
                    TextView desc = findViewById(R.id.view_desc);
                    TextView source = findViewById(R.id.view_source);
                    final TextView collaborators = findViewById(R.id.authors_col);

                    collaborators.setText("");

                    for (DataSnapshot collaborator: snapshot.child("collaborators").getChildren()) {
                        collaborators.setText(collaborators.getText().toString().concat(" ").concat(Util.key2name.get(collaborator.getValue(String.class))));
                    }

                    appname.setText(snapshot.child("name").getValue(String.class));
                    desc.setText(snapshot.child("desc").getValue(String.class));
                    source.setText(snapshot.child("open").getValue(Boolean.class) ? "Open-sourced" : "Not Open-sourced");

                    if (!snapshot.child("open").getValue(Boolean.class)) {
                        MaterialButton clone = findViewById(R.id.view_clone_button);
                        clone.setText("Request");
                        clone.setIcon(getDrawable(R.drawable.ic_baseline_people_24));
                    }

                    if (snapshot.child("author").getValue(String.class).equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                        MaterialButton clone = findViewById(R.id.view_clone_button);
                        clone.setText("Delete project");
                        clone.setIcon(getDrawable(R.drawable.ic_delete));
                        clone.setBackgroundColor(ContextCompat.getColor(ViewOnlineProjectActivity.this, R.color.colorAccent));
                    }

                    database.getReference("userdata/" + snapshot.child("author").getValue(String.class) + "/name")
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()) {
                                        author.setText(snapshot.getValue(String.class));
                                        collaborators.setText(snapshot.getValue(String.class).concat(" ").concat(collaborators.getText().toString()));
                                        findViewById(R.id.contentLoading_view).setVisibility(View.GONE);
                                    } else {
                                        Toast.makeText(ViewOnlineProjectActivity.this, "Error: Author does not exist", Toast.LENGTH_LONG)
                                                .show();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Toast.makeText(ViewOnlineProjectActivity.this, "Error: Cannot get author's name", Toast.LENGTH_LONG)
                                            .show();
                                }
                            });

                } else {
                    Toast.makeText(ViewOnlineProjectActivity.this, "Error: Project doesn't exist", Toast.LENGTH_LONG)
                    .show();
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}