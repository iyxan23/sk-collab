package com.iyxan23.sketch.collab.helpers;

import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SpannableHelper {


    public static void span_regex(int color, SpannableString data, String regex, int regex_flags) {
        Pattern p;

        if (regex_flags != -1) {
            p = Pattern.compile(regex, regex_flags);
        } else {
            p = Pattern.compile(regex);
        }

        Matcher m = p.matcher(data);

        while (m.find()) {

            data.setSpan(new ForegroundColorSpan(color), m.start(), m.end(), SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    public static void span_regex(int color, SpannableString data, String regex) {
        span_regex(color, data, regex, -1);
    }
}
