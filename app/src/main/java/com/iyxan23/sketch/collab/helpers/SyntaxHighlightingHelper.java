package com.iyxan23.sketch.collab.helpers;

import android.graphics.Color;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;

import java.util.regex.Pattern;

public class SyntaxHighlightingHelper {

    public static SpannableStringBuilder highlight_patch(String data) {
        SpannableStringBuilder builder = new SpannableStringBuilder(data);

        // Highlight the @@ thing
        builder = SpannableHelper.span_regex(new ForegroundColorSpan(Color.MAGENTA), builder, "@@");

        // Highlight the +
        builder = SpannableHelper.span_regex(new ForegroundColorSpan(Color.GREEN), builder, "^\\+.+", Pattern.MULTILINE);

        // Highlight the -
        builder = SpannableHelper.span_regex(new ForegroundColorSpan(Color.GREEN), builder, "^-.+", Pattern.MULTILINE);

        return builder;
    }

    public static SpannableStringBuilder highlight_logic(String data) {
        SpannableStringBuilder builder = new SpannableStringBuilder(data);

        // Highlight the title's activity name
        builder = SpannableHelper.span_regex(new ForegroundColorSpan(0xFFD19A66), builder, "^@\\w+", Pattern.MULTILINE);

        // Highlight the title's type
        builder = SpannableHelper.span_regex(new ForegroundColorSpan(0xFFB856D8), builder, "\\.java_.+", Pattern.MULTILINE);

        // Highlight the variable types
        builder = SpannableHelper.span_regex(new ForegroundColorSpan(0xFFD19A66), builder, "^\\w+:", Pattern.MULTILINE);

        // Highlight the variable names
        builder = SpannableHelper.span_regex(new ForegroundColorSpan(0xFFB59448), builder, ":\\w+$", Pattern.MULTILINE);

        // Highlight the string in json
        builder = SpannableHelper.span_regex(new ForegroundColorSpan(0xFF89CA78), builder, "\"\\w*\"");

        // Highlight the %s, %m.view thingy
        builder = SpannableHelper.span_regex(new ForegroundColorSpan(0xFFE5C07B), builder, "%[a-z]\\.(\\w+)|%[a-z]");

        return builder;
    }
}
