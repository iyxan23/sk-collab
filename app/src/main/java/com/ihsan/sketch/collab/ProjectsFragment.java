package com.ihsan.sketch.collab;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.Toast;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Objects;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class ProjectsFragment extends Fragment {

    private static final String TAG = "ProjectsActivity";
    private ArrayList<SketchwareProject> swproj;

    public ProjectsFragment(ArrayList<SketchwareProject> swproj) {
        this.swproj = swproj;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.projects_fragment, container, false);

        View notfound = view.findViewById(R.id.img_no_project);
        View notfound1 = view.findViewById(R.id.no_projects);

        RecyclerView projects = view.findViewById(R.id.recyclerview_projects);

        if (swproj.size() == 0) {
            projects.setVisibility(View.GONE);
        } else {
            notfound.setVisibility(View.GONE);
            notfound1.setVisibility(View.GONE);
            projects.setAdapter(new RecyclerViewSketchwareProjectsAdapter(swproj, getActivity()));
            projects.setLayoutManager(new LinearLayoutManager(getContext()));
            projects.setHasFixedSize(true);
        }

        return view;
    }

    /*

    private FirebaseDatabase database;
    RecyclerViewSketchwareProjectsAdapter projectsAdapter;
    ArrayList<SketchwareProject> data = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.projects_fragment);

        database = FirebaseDatabase.getInstance();
        getSupportActionBar().setTitle(getString(R.string.projects));
        getSupportActionBar().setIcon(getDrawable(R.drawable.ic_baseline_keyboard_backspace_24));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        try {
            storagePerms();
        } catch (Exception e) {
            e.printStackTrace();
            Snackbar.make(findViewById(R.id.parent_projects), e.getMessage(), Snackbar.LENGTH_LONG)
                    .setBackgroundTint(Color.RED)
                    .show();
        }

        RecyclerView rv = findViewById(R.id.recyclerview_projects);
        projectsAdapter = new RecyclerViewSketchwareProjectsAdapter(data, this);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(projectsAdapter);

        // Check if empty
        if (data.size() != 0) {
            Log.d(TAG, "onCreate: " + data.toString());
            findViewById(R.id.no_projects).setVisibility(View.GONE);
            findViewById(R.id.img_no_project).setVisibility(View.GONE);
        } else {
            rv.setVisibility(View.GONE);
        }

        final SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.swipe_projects);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                try {
                    getSketchwareProjects();
                } catch (JSONException e) {
                    e.printStackTrace();
                    Snackbar.make(findViewById(R.id.parent_projects), e.getMessage(), Snackbar.LENGTH_LONG)
                            .setBackgroundTint(Color.RED)
                            .show();
                }
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void storagePerms() throws JSONException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission("android.permission.READ_EXTERNAL_STORAGE") == PackageManager.PERMISSION_DENIED ||
                    checkSelfPermission("android.permission.WRITE_EXTERNAL_STORAGE") == PackageManager.PERMISSION_DENIED) {
                requestPermissions(new String[]{"android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE"}, 1000);
            } else {
                getSketchwareProjects();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1000) {
            try {
                getSketchwareProjects();
            } catch (JSONException e) {
                e.printStackTrace();
                Snackbar.make(findViewById(R.id.parent_projects), e.getMessage(), Snackbar.LENGTH_LONG)
                        .setBackgroundTint(Color.RED)
                        .show();
            }
        }
    }

    private void getSketchwareProjects() throws JSONException {
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/.sketchware/mysc/list/";
        Log.d(TAG, "getSketchwareProjects: " +path);
        for (String pat: listDir(path)) {
            try {
                Log.d(TAG, "getSketchwareProjects: " + pat + "/project");
                JSONObject project = new JSONObject(decrypt(pat + "/project"));
                SketchwareProject sw_proj = new SketchwareProject(
                        project.getString("my_app_name"),
                        project.getString("sc_ver_name"),
                        project.getString("my_sc_pkg_name"),
                        project.getString("my_ws_name"),
                        project.getString("sc_id"));
                Toast.makeText(this, project.getString("my_app_name"), Toast.LENGTH_SHORT);
                data.add(sw_proj);
            } catch (Exception e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG);
                Log.e(TAG, "getSketchwareProjects: " + e.toString());
            }
        }
    }

    private String decrypt(String path) {
        try {
            Cipher instance = Cipher.getInstance("AES/CBC/PKCS5Padding");;
            byte[] bytes = "sketchwaresecure".getBytes();
            instance.init(2, new SecretKeySpec(bytes, "AES"), new IvParameterSpec(bytes));
            RandomAccessFile randomAccessFile = new RandomAccessFile(path, "r");
            byte[] bArr = new byte[((int) randomAccessFile.length())];
            randomAccessFile.readFully(bArr);
            return new String(instance.doFinal(bArr));
        } catch (Exception e) {
            Snackbar.make(findViewById(R.id.parent_projects), e.getMessage(), Snackbar.LENGTH_LONG)
                    .setBackgroundTint(Color.RED)
                    .show();
        }
        return "ERROR WHILE PARSING";
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_left, R.anim.slide_out_right);
    }

    @Override
    public void onBackPressed() {
        super.finish();
        overridePendingTransition(R.anim.slide_left, R.anim.slide_out_right);
    }

    public ArrayList<String> listDir(String str) {
        ArrayList<String> arrayList = new ArrayList<>();
        File file = new File(str);
        if (file.exists() && !file.isFile()) {
            File[] listFiles = file.listFiles();
            if (listFiles != null && listFiles.length > 0) {
                arrayList.clear();
                for (File absolutePath : listFiles) {
                    arrayList.add(absolutePath.getAbsolutePath());
                }
            }
        }
        return arrayList;
    }
     */

}