package com.iyxan23.sketch.collab.online;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.NotNull;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
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

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        CollectionReference projects = firestore.collection("projects");

        // TODO: RECYCLERVIEW PAGINATION
        projects.get()
                .addOnCompleteListener(task -> {
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
                                        0  // Temporarily Hardcoded
                                )
                        );
                    }

                    if (savedInstanceState != null) savedInstanceState.putParcelableArrayList("items", items);

                    adapter.updateView(items);
                });
    }
}
