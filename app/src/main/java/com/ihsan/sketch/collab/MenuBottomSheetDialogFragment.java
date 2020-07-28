package com.ihsan.sketch.collab;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.Collections;

public class MenuBottomSheetDialogFragment extends BottomSheetDialogFragment {

    public static MenuBottomSheetDialogFragment newInstance() {
        return new MenuBottomSheetDialogFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_menu, container,false);
        view.setBackgroundColor(Color.TRANSPARENT);

        View profile = view.findViewById(R.id.profile_bsd);
        View projects = view.findViewById(R.id.projects_bsd);
        View online_projects = view.findViewById(R.id.online_projects_bsd);
        View collaborate = view.findViewById(R.id.collaborate_bsd);
        View settings = view.findViewById(R.id.settings_bsd);
        final Activity mActivity = getActivity();

        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(mActivity, ProfileActivity.class);
                startActivity(i);
                mActivity.finish();
            }
        });

        projects.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(mActivity, ProjectsActivity.class);
                startActivity(i);
                mActivity.finish();
            }
        });

        online_projects.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(mActivity, MainActivity.class);
                startActivity(i);
                mActivity.finish();
            }
        });

        collaborate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(mActivity, CollaborateActivity.class);
                startActivity(i);
                mActivity.finish();
            }
        });

        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(mActivity, SettingsActivity.class);
                startActivity(i);
                mActivity.finish();
            }
        });

        return view;
    }
}

