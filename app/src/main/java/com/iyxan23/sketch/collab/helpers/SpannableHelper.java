package com.iyxan23.sketch.collab.helpers;

import android.text.SpannableString;
import android.text.SpannableStringBuilder;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SpannableHelper {

    public static SpannableStringBuilder span_regex(Object what, SpannableStringBuilder data, String regex, int regex_flags) {
        SpannableStringBuilder result = new SpannableStringBuilder(data);

        Pattern p;

        if (regex_flags != -1) {
            p = Pattern.compile(regex, regex_flags);
        } else {
            p = Pattern.compile(regex);
        }

        Matcher m = p.matcher(data);

        while (m.find()) {
            result.setSpan(what, m.start(), m.end(), SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        return result;
    }

    public static SpannableStringBuilder span_regex(Object what, SpannableStringBuilder data, String regex) {
        return span_regex(what, data, regex, -1);
    }
}
