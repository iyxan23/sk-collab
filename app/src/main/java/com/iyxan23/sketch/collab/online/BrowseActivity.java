package com.iyxan23.sketch.collab.online;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.iyxan23.sketch.collab.R;
import com.iyxan23.sketch.collab.adapters.BrowseItemAdapter;
import com.iyxan23.sketch.collab.models.BrowseItem;

import java.util.ArrayList;

public class BrowseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse);

        RecyclerView rv = findViewById(R.id.browse_rv);
        rv.setLayoutManager(new LinearLayoutManager(this));

        BrowseItemAdapter adapter = new BrowseItemAdapter(this);
        rv.setAdapter(adapter);

        if (savedInstanceState != null) {
            if (!savedInstanceState.isEmpty()) {
                adapter.updateView(savedInstanceState.getParcelableArrayList("items"));
                return;
            }
        }

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        CollectionReference projects = firestore.collection("projects");

        // TODO: RECYCLERVIEW PAGINATION
        projects.get()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Toast.makeText(BrowseActivity.this, "An error occured while retrieving data: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        return;
                    }

                    ArrayList<BrowseItem> items = new ArrayList<>();

                    for (DocumentSnapshot project: task.getResult().getDocuments()) {
                        items.add(
                                new BrowseItem(
                                        project.getId(),
                                        project.getString("author"),
                                        project.getString("name"),
                                        project.getString("author"),
                                        // TODO: THIS
                                        // project.getTimestamp("last_updated_timestamp") == null ? project.child("last_updated_timestamp").getValue(int.class) : 0
                                        new Timestamp(0,0)  // Temporarily Hardcoded
                                )
                        );
                    }

                    if (savedInstanceState != null) savedInstanceState.putParcelableArrayList("items", items);

                    adapter.updateView(items);
                });
    }
}
