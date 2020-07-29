package com.ihsan.sketch.collab;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainFragment extends Fragment {
    private static final String TAG = "MainFragment";
    private FirebaseDatabase database;
    private FirebaseAuth auth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        DatabaseReference userdata = database.getReference("userdata");

        if (user == null) {
            // Mod user detected
            Intent i = new Intent(getActivity(), LoginActivity.class);
            startActivity(i);
            getActivity().finish();
        }

        final TextView welcome = view.findViewById(R.id.welcome_home);
        final TextView name = view.findViewById(R.id.fullname_home);
        TextView email = view.findViewById(R.id.email_home);

        email.setText(user.getEmail());

        userdata.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child("name").exists()) {
                    welcome.setText(getString(R.string.welcome).concat("! ").concat(snapshot.child("name").getValue(String.class)));
                    name.setText(snapshot.child("name").getValue(String.class));
                } else {
                    welcome.setText(getString(R.string.welcome).concat("!"));
                    name.setText(getString(R.string.err_set_name));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        return view;
    }
}