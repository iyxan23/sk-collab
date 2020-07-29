package com.ihsan.sketch.collab;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;

public class HomeActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        final MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Home");

        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView nv = findViewById(R.id.navview);
        nv.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.drawer_home:
                        getSupportFragmentManager().beginTransaction().replace(R.id.framelayout,
                                new MainFragment()).commit();
                        toolbar.setTitle("Home");
                        break;
                    case R.id.drawer_projects:
                        getSupportFragmentManager().beginTransaction().replace(R.id.framelayout,
                                new ProjectsFragment()).commit();
                        toolbar.setTitle("Projects");
                        break;
                    case R.id.drawer_online_proj:
                        getSupportFragmentManager().beginTransaction().replace(R.id.framelayout,
                                new OnlineFragment()).commit();
                        toolbar.setTitle("Online Projects");
                        break;
                    case R.id.drawer_collaborate:
                        getSupportFragmentManager().beginTransaction().replace(R.id.framelayout,
                                new CollaborateFragment()).commit();
                        toolbar.setTitle("Collaborate");
                        break;
                    case R.id.drawer_share:
                        Intent sendIntent = new Intent();
                        sendIntent.setAction(Intent.ACTION_SEND);
                        sendIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_text));
                        sendIntent.setType("text/plain");
                        Intent shareIntent = Intent.createChooser(sendIntent, null);
                        startActivity(shareIntent);
                        break;
                    default:
                        Toast.makeText(HomeActivity.this, "ERROR", Toast.LENGTH_LONG);
                }
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open_drawer, R.string.close_drawer);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.framelayout,
                    new MainFragment()).commit();
            nv.setCheckedItem(R.id.drawer_home);
        }

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    drawerLayout.openDrawer(GravityCompat.START);
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}