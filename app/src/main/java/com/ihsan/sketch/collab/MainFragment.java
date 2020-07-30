package com.ihsan.sketch.collab;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityOptionsCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

public class MainFragment extends Fragment {
    private static final String TAG = "MainFragment";

    private FirebaseUser user;
    private String name;
    private View.OnClickListener nameClick;
    private ArrayList<SketchwareProject> offlineProjects;
    private NavigationView nv;
    private MaterialToolbar toolbar;

    public MainFragment(FirebaseUser user, @Nullable String name, @Nullable View.OnClickListener nameClick, ArrayList<SketchwareProject> swprojs, NavigationView nv, MaterialToolbar toolbar) {
        this.user = user;
        this.name = name;
        this.nameClick = nameClick;
        this.offlineProjects = swprojs;
        this.nv = nv;
        this.toolbar = toolbar;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        TextView welcome = view.findViewById(R.id.welcome_home);
        TextView name = view.findViewById(R.id.fullname_home);
        TextView email = view.findViewById(R.id.email_home);
        RecyclerView offline = view.findViewById(R.id.rv_offline_proj_home);

        View openOffline = view.findViewById(R.id.open_more_offline_proj_home);
        openOffline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nv.setCheckedItem(R.id.drawer_projects);
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.framelayout,
                        new ProjectsFragment(offlineProjects)).commit();
                toolbar.setTitle("Projects");
            }
        });

        offline.setAdapter(new RecyclerViewSketchwareProjectsAdapter(offlineProjects, getActivity()));
        offline.setLayoutManager(new LinearLayoutManager(getContext()) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        });
        offline.setHasFixedSize(true);

        view.findViewById(R.id.btn_no_online_projects).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getActivity(), UploadActivity.class);
                ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(), view.findViewById(R.id.btn_no_online_projects), "upload_transition");
                getActivity().startActivity(i, optionsCompat.toBundle());
            }
        });

        if (offlineProjects.size() == 0) {
            view.findViewById(R.id.no_sw_proj_detected).setVisibility(View.VISIBLE);
            offline.setVisibility(View.GONE);
        }

        if (this.name != null) {
            name.setText(this.name);
            welcome.setText(getString(R.string.welcome).concat("! ").concat(this.name));
        } else {
            name.setText(getString(R.string.err_set_name));
            name.setOnClickListener(nameClick);
            welcome.setText(getString(R.string.welcome).concat("!"));
        }
        email.setText(user.getEmail());
        return view;
    }
}