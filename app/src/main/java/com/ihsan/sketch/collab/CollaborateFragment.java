package com.ihsan.sketch.collab;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

public class CollaborateFragment extends Fragment {

    private ArrayList<OnlineProject> onlineProjects;

    public CollaborateFragment(ArrayList<OnlineProject> onlineProjects) {
        this.onlineProjects = onlineProjects;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_collaborate, container, false);

        RecyclerView rv = view.findViewById(R.id.col_rv);
        rv.setAdapter(new RecyclerViewCollaborateProjectAdapter(onlineProjects));
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setHasFixedSize(true);

        return view;
    }
}