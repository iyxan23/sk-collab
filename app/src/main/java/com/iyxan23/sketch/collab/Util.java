package com.iyxan23.sketch.collab;

import android.util.Base64;

public class Util {
    public static String base64encode(String txt) {
        byte[] data = txt.getBytes();
        return Base64.encodeToString(data, Base64.DEFAULT);
    }

    public static String base64decode(String base64) {
        byte[] data = Base64.decode(base64, Base64.DEFAULT);
        return new String(data);
    }
}
