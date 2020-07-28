package com.ihsan.sketch.collab;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.Toast;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class ProjectsActivity extends AppCompatActivity {

    private ArrayList<SketchwareProject> proj_ids;
    private FirebaseDatabase database;
    RecyclerViewSketchwareProjectsAdapter projectsAdapter;
    ArrayList<SketchwareProject> data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_projects);

        database = FirebaseDatabase.getInstance();
        getSupportActionBar().setTitle(getString(R.string.projects));
        getSupportActionBar().setIcon(getDrawable(R.drawable.ic_baseline_keyboard_backspace_24));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        storagePerms();
        Toast.makeText(this, proj_ids.toString(), Toast.LENGTH_LONG).show();

        RecyclerView rv = findViewById(R.id.recyclerview_projects);
        projectsAdapter = new RecyclerViewSketchwareProjectsAdapter(data, this);
        rv.setLayoutAnimation(AnimationUtils.loadLayoutAnimation(this, R.anim.recyclerviewanim));
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(projectsAdapter);

        try {
            getSketchwareProjects();
        } catch (Exception e) {
            e.printStackTrace();
            Snackbar.make(findViewById(R.id.parent_projects), e.getMessage(), Snackbar.LENGTH_LONG)
                    .setBackgroundTint(Color.RED)
                    .show();
        }

        projectsAdapter.updateView(data);
        projectsAdapter.notifyDataSetChanged();
    }

    private void storagePerms() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission("android.permission.READ_EXTERNAL_STORAGE") == PackageManager.PERMISSION_DENIED ||
                    checkSelfPermission("android.permission.WRITE_EXTERNAL_STORAGE") == PackageManager.PERMISSION_DENIED) {
                requestPermissions(new String[]{"android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE"}, 1000);
            }
        }
    }

    private void getSketchwareProjects() throws JSONException {
        String path = Environment.getExternalStorageDirectory() + "/.sketchware/mysc/list/";
        File file = new File(path);
        if (file.exists() && !file.isFile()) {
            File[] listFiles = file.listFiles();
            for (File file_ : listFiles) {
                if (file_.isDirectory()) {
                    JSONObject project = new JSONObject(decrypt(file_.getAbsolutePath().concat("/project")));
                    SketchwareProject sw_proj = new SketchwareProject(
                            project.getString("my_app_name"),
                            project.getString("sc_ver_name"),
                            project.getString("my_sc_pkg_name"),
                            project.getString("my_ws_name"),
                            project.getString("sc_id"));
                    proj_ids.add(sw_proj);
                }
            }
        }
    }

    private String decrypt(String path) {
        try {
            Cipher instance = null;
            instance = Cipher.getInstance("AES/CBC/PKCS5Padding");
            byte[] bytes = "sketchwaresecure".getBytes();
            instance.init(2, new SecretKeySpec(bytes, "AES"), new IvParameterSpec(bytes));
            RandomAccessFile randomAccessFile = new RandomAccessFile(path, "r");
            byte[] bArr = new byte[((int) randomAccessFile.length())];
            randomAccessFile.readFully(bArr);
            return new String(instance.doFinal(bArr));
        } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException | IllegalBlockSizeException | InvalidKeyException | IOException | NoSuchPaddingException | BadPaddingException e) {
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
}