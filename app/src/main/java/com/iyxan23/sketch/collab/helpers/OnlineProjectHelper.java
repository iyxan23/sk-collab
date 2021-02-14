package com.iyxan23.sketch.collab.helpers;

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
import com.iyxan23.sketch.collab.models.SketchwareProject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
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
}
