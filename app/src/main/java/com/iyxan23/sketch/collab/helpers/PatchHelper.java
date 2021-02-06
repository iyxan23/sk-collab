package com.iyxan23.sketch.collab.helpers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PatchHelper {

    public static String reverse_patch(String patch) {
        Pattern plus_pattern = Pattern.compile("^\\+", Pattern.MULTILINE);
        Pattern minus_pattern = Pattern.compile("^-", Pattern.MULTILINE);

        StringBuilder patch_sb = new StringBuilder(patch);

        // Flip + to -
        Matcher matcher = plus_pattern.matcher(patch);
        while (matcher.find()) {
            patch_sb.setCharAt(matcher.start(), '-');
        }

        // Flip - to +
        matcher = minus_pattern.matcher(patch);
        while (matcher.find()) {
            patch_sb.setCharAt(matcher.start(), '+');
        }

        return patch_sb.toString();
    }
}
