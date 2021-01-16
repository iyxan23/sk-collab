package com.iyxan23.sketch.collab.online;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.NotNull;
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
        DatabaseReference projects = database.getReference("/projects");

        projects.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                ArrayList<BrowseItem> items = new ArrayList<>();

                for (DataSnapshot project: snapshot.getChildren()) {
                    items.add(
                            new BrowseItem(
                                    project.getKey(),
                                    project.child("author").getValue(String.class),
                                    project.child("name").getValue(String.class),
                                    project.child("author").getValue(String.class),
                                    // TODO: THIS
                                    project.child("last_updated_timestamp").exists() ? project.child("last_updated_timestamp").getValue(int.class) : 0
                            )
                    );
                }

                if (savedInstanceState != null) savedInstanceState.putParcelableArrayList("items", items);

                adapter.updateView(items);
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }
}
