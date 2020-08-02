package com.ihsan.sketch.collab;

import android.content.Intent;
import android.os.Bundle;

import androidx.core.app.ActivityOptionsCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class OnlineFragment extends Fragment {

    ArrayList<OnlineProject> all_projects;

    public OnlineFragment(ArrayList<OnlineProject> all_projects) {
        this.all_projects = all_projects;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_online, container, false);

        RecyclerView rv = view.findViewById(R.id.rv_online);
        final FloatingActionButton upload = view.findViewById(R.id.fab_upload_online);

        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getActivity(), UploadActivity.class);
                ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeClipRevealAnimation(upload, 0, 0, upload.getMeasuredWidth(), upload.getMeasuredHeight());
                startActivity(i, optionsCompat.toBundle());
            }
        });

        rv.setAdapter(new RecyclerViewOnlineProjectsAdapter(all_projects, getActivity()));
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setHasFixedSize(true);

        return view;
    }
}