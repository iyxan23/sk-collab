package com.ihsan.sketch.collab;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Bundle;
import android.transition.Explode;
import android.view.View;
import android.view.Window;
import android.widget.Button;
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

    String project_key;
    String project_name;
    String project_author;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);
        getWindow().setEnterTransition(new Explode());
        getWindow().setExitTransition(new Explode());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_online_project);

        getSupportActionBar().setTitle("View Project");
        getSupportActionBar().setElevation(0);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Button clone_button = findViewById(R.id.view_clone_button);

        Intent i = getIntent();
        project_key = i.getStringExtra("key");

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("/projects/" + project_key);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    TextView appname = findViewById(R.id.view_appname);
                    final TextView author  = findViewById(R.id.view_author);
                    TextView desc = findViewById(R.id.view_desc);
                    TextView source = findViewById(R.id.view_source);
                    final TextView collaborators = findViewById(R.id.authors_col);

// Coming soon
//                    for (DataSnapshot collaborator: snapshot.child("collaborators").getChildren()) {
//                        collaborators.setText(collaborators.getText().toString().concat(" ").concat(Util.key2name.get(collaborator.getValue(String.class))));
//                    }

                    collaborators.setText(snapshot.child("author").getValue(String.class));
                    appname.setText(snapshot.child("name").getValue(String.class));
                    desc.setText(snapshot.child("desc").getValue(String.class));
                    source.setText(snapshot.child("open").getValue(Boolean.class) ? "Open-sourced" : "Not Open-sourced");

                    project_name = snapshot.child("name").getValue(String.class);
                    project_author = snapshot.child("author").getValue(String.class);

                    if (snapshot.child("open").getValue(Boolean.class)) {
                        // Public project, clone
                        MaterialButton clone = findViewById(R.id.view_clone_button);
                        clone.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                cloneProject();
                            }
                        });
                    } else {
                        MaterialButton clone = findViewById(R.id.view_clone_button);
                        clone.setEnabled(false);
                    }

                    if (snapshot.child("author").getValue(String.class).equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                        // Own Project
                        MaterialButton clone = findViewById(R.id.view_clone_button);
                        clone.setText("Delete project");
                        clone.setIcon(getDrawable(R.drawable.ic_delete));
                        clone.setBackgroundColor(ContextCompat.getColor(ViewOnlineProjectActivity.this, R.color.colorAccent));
                        clone.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                deleteProject();
                            }
                        });
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

    private void cloneProject() {
        Intent i = new Intent(getApplicationContext(), CloneActivity.class);
        i.putExtra("key", project_key);
        i.putExtra("author", project_author);
        i.putExtra("name", project_name);
        startActivity(i);
    }

    private void deleteProject() {
        Toast.makeText(this, "delete unfinished", Toast.LENGTH_LONG).show();
    }
}