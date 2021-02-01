package com.iyxan23.sketch.collab.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.iyxan23.sketch.collab.R;
import com.iyxan23.sketch.collab.Util;
import com.iyxan23.sketch.collab.models.Commit;
import com.iyxan23.sketch.collab.models.SketchwareProject;
import com.iyxan23.sketch.collab.online.ViewOnlineProjectActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.ExecutionException;

import name.fraser.neil.plaintext.diff_match_patch;

public class CloneService extends Service {

    private static final String TAG = "CloneService";

    private final String CLONE_CHANNEL_ID = "CLONE_CHANNEL";
    private final int NOTIFICATION_ID = 1;

    String project_key;
    String project_name;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        project_key = intent.getStringExtra("project_key");
        project_name = intent.getStringExtra("project_name");

        Intent notificationIntent = new Intent(this, ViewOnlineProjectActivity.class);
        notificationIntent.putExtra("project_key", project_key);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, notificationIntent, 0);

        final Notification.Builder notification_builder;

        // Channel ID is for 26+ / Android 8+ / Android Oreo+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createCloneNotificationChannel();

            notification_builder = new Notification.Builder(this, CLONE_CHANNEL_ID);
        } else {
            notification_builder = new Notification.Builder(this);
        }

        Notification notification = notification_builder
                                        .setContentTitle("Cloning Project")
                                        .setContentText("Cloning " + project_name + " (" + project_key + ")")
                                        .setProgress(100, 1, true)
                                        .setSmallIcon(R.drawable.ic_file_download)
                                        .setContentIntent(pendingIntent)
                                        .build();

        startForeground(NOTIFICATION_ID, notification);

        // Start the cloning on a different thread
        Toast.makeText(CloneService.this, "Cloning started.", Toast.LENGTH_SHORT).show();
        new Thread(() -> {
            try {
                FirebaseFirestore firestore = FirebaseFirestore.getInstance();
                DocumentReference   project = firestore.collection("projects").document(project_key);
                CollectionReference project_snapshot = firestore.collection("projects").document(project_key).collection("snapshot");
                CollectionReference project_commits  = firestore.collection("projects").document(project_key).collection("commits");

                // Project data in their decrypted string format
                HashMap<String, String> project_data = new HashMap<>();

                String[] keys = new String[] {"mysc_project", "logic", "view", "library", "resource", "file"};

                // Get the snapshot, get the commits, and apply the commits to the snapshot
                DocumentSnapshot project_metadata = Tasks.await(project.get());
                QuerySnapshot snapshot = Tasks.await(project_snapshot.get());
                QuerySnapshot commits  = Tasks.await(project_commits .orderBy("timestamp", Query.Direction.ASCENDING).get());

                for (DocumentSnapshot doc: snapshot.getDocuments()) {
                    project_data.put(doc.getId(), Util.decrypt(doc.getBlob("data").toBytes()));
                }

                diff_match_patch dmp = new diff_match_patch();
                // Apply the patch
                for (DocumentSnapshot commit: commits) {
                    HashMap<String, String> patch = (HashMap<String, String>) commit.get("patch");

                    if (patch == null) continue;

                    for (String key: keys) {
                        if (!patch.containsKey(key)) continue;

                        LinkedList<diff_match_patch.Patch> patches = (LinkedList<diff_match_patch.Patch>) dmp.patch_fromText(patch.get(key));
                        // TODO: CHECK PATCH STATUSES
                        Object[] result = dmp.patch_apply(patches, project_data.get(key));

                        project_data.put(key, (String) result[0]);
                    }

                    // Easier to understand version:
                    /*
                    if (c.patch.containsKey("mysc_project")) {
                        LinkedList<diff_match_patch.Patch> patches = (LinkedList<diff_match_patch.Patch>) dmp.patch_fromText(c.patch.get("mysc_project"));
                        Object[] result = dmp.patch_apply(patches, c.patch.get("mysc_project"));

                        // Check if they're successful or not
                        boolean[] checks = (boolean[]) result[1];

                        for (boolean check : checks) {
                            if (!check) {
                                Toast.makeText(CloneService.this, "Failed to patch", Toast.LENGTH_SHORT).show();
                                stopSelf();
                            }
                        }

                        project_data.put("mysc_project", (String) result[0]);
                    } else if (c.patch.containsKey("view")) {
                        ...
                    } else if (c.patch.containsKey("logic")) {
                        ...
                    } else if (...
                     */
                }

                int free_id = Util.getLatestId();

                // Alter the mysc project
                JSONObject project_json = new JSONObject(project_data.get("mysc_project"));

                project_json.put("sc_id", free_id);

                project_json.put("sk-collab-project-key", project_key);
                project_json.put("sk-collab-owner", project_metadata.getString("author"));
                project_json.put("sk-collab-latest-commit", commits.getDocuments().get(commits.getDocuments().size() - 1).getId());
                project_json.put("sk-collab-project-visibility", "public");

                // Put it back
                project_data.put("mysc_project", project_json.toString());

                // And save it
                new SketchwareProject(
                        Util.encrypt(project_data.get("logic").getBytes()),
                        Util.encrypt(project_data.get("view").getBytes()),
                        Util.encrypt(project_data.get("resource").getBytes()),
                        Util.encrypt(project_data.get("library").getBytes()),
                        Util.encrypt(project_data.get("file").getBytes()),
                        Util.encrypt(project_data.get("mysc_project").getBytes())
                ).applyChanges();

            } catch (InterruptedException e) {
                // Restore interrupt status.
                Thread.currentThread().interrupt();
            } catch (ExecutionException | JSONException | IOException e) {
                e.printStackTrace();

                // So it will appear on the debug page
                throw new RuntimeException(e);
            }

            // Stop the service using the startId, so that we don't stop
            // the service in the middle of handling another job
            stopSelf();
        }).start();

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private void createCloneNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        CharSequence name = "Clone Project";
        String description = "This notification will appear when you clone a project in SketchCollab.";
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = new NotificationChannel(CLONE_CHANNEL_ID, name, importance);
        channel.setDescription(description);
        // Register the channel with the system; you can't change the importance
        // or other notification behaviors after this
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }
}
