package com.iyxan23.sketch.collab.helpers;

import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.iyxan23.sketch.collab.Util;
import com.iyxan23.sketch.collab.models.Commit;
import com.iyxan23.sketch.collab.models.SketchwareProject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import name.fraser.neil.plaintext.diff_match_patch;

public class OnlineProjectHelper {
    public static SketchwareProject querySnapshotToSketchwareProject(QuerySnapshot snapshot) {
        SketchwareProject project = new SketchwareProject();
        // Loop through the document and get every data/ files
        for (DocumentSnapshot doc_snapshot : snapshot.getDocuments()) {
            if (doc_snapshot.getId().equals("logic")) {
                project.logic = doc_snapshot.getBlob("data").toBytes();

            } else if (doc_snapshot.getId().equals("view")) {
                project.view = doc_snapshot.getBlob("data").toBytes();

            } else if (doc_snapshot.getId().equals("file")) {
                project.file = doc_snapshot.getBlob("data").toBytes();

            } else if (doc_snapshot.getId().equals("mysc_project")) {
                project.mysc_project = doc_snapshot.getBlob("data").toBytes();

            } else if (doc_snapshot.getId().equals("library")) {
                project.library = doc_snapshot.getBlob("data").toBytes();

            } else if (doc_snapshot.getId().equals("resource")) {
                project.resource = doc_snapshot.getBlob("data").toBytes();
            }
        }

        return project;
    }

    public static boolean hasPermission(DocumentSnapshot project_data) {
        ArrayList<String> members = (ArrayList<String>) project_data.get("members");
        String author = project_data.getString("author");

        if (author == null) {
            return false;
        }

        FirebaseAuth auth = FirebaseAuth.getInstance();

        // Fallback to an empty arraylist if members is null / doesn't exist
        // (To avoid NPE)
        members = members == null ? new ArrayList<>() : members;

        // Check if user is an author of this project
        if (!author.equals(auth.getUid())) {
            // Nop he's not, check if he's a member or not.
            return members.contains(auth.getUid());
        }

        return true;
    }

    @WorkerThread
    public static Map<String, String> fetch_project_latest_commit(String project_key) throws ExecutionException, InterruptedException {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        CollectionReference project_snapshot = firestore.collection("projects").document(project_key).collection("snapshot");
        CollectionReference project_commits  = firestore.collection("projects").document(project_key).collection("commits");

        // Project data in their decrypted string format
        HashMap<String, String> project_data = new HashMap<>();

        String[] keys = new String[] {"mysc_project", "logic", "view", "library", "resource", "file"};

        // Get the snapshot, get the commits, and apply the commits to the snapshot
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

        return project_data;
    }

    /**
     * This function converts a List<\DocumentSnapshot\> into an ArrayList<\Commit\>
     *
     * @param commits The commits
     * @param with_usernames Do you want username in them? This variable is used to reduce internet usage
     * @return Converted arraylist of commits
     */
    @Nullable
    @WorkerThread
    public static ArrayList<Commit> convert_document_snapshots_into_commits(List<DocumentSnapshot> commits, boolean with_usernames) {
        ArrayList<Commit> commits_ = new ArrayList<>();
        HashMap<String, String> cached_usernames = new HashMap<>();

        for (DocumentSnapshot commit: commits) {
            String username = null;
            String author_uid = commit.getString("author");

            if (with_usernames) {
                if (!cached_usernames.containsKey(author_uid)) {
                    DocumentReference userdata = FirebaseFirestore.getInstance().collection("userdata").document(author_uid);

                    try {
                        DocumentSnapshot userdata_snapshot = Tasks.await(userdata.get());
                        username = userdata_snapshot.getString("name");

                        cached_usernames.put(author_uid, username);

                    } catch (ExecutionException | InterruptedException e) {
                        e.printStackTrace();
                        return null;
                    }

                } else {
                    username = cached_usernames.get(author_uid);
                }
            }

            Commit c = new Commit();

            c.id = commit.getId();
            if (with_usernames) c.author_username = username;
            c.author = commit.getString("author");
            c.name = commit.getString("name");
            c.sha512sum = commit.getString("sha512sum");
            c.patch = (Map<String, String>) commit.get("patch");
            c.timestamp = commit.getTimestamp("timestamp");

            commits_.add(c);
        }

        return commits_;
    }
}
