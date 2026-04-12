package com.arti405.setnote.util;

import android.content.Context;
import android.content.SharedPreferences;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateFormatHelper {

    private static final String PREFS_NAME = "setnote_prefs";
    private static final String KEY_DATE_FORMAT = "date_format";

    public static final String FORMAT_1 = "dd.MM.yyyy";
    public static final String FORMAT_2 = "yyyy-MM-dd";
    public static final String FORMAT_3 = "MMM dd, yyyy";

    public static String getSavedFormat(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_DATE_FORMAT, FORMAT_1);
    }

    public static void saveFormat(Context context, String format) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_DATE_FORMAT, format).apply();
    }

    public static String formatDate(Context context, long millis) {
        String pattern = getSavedFormat(context);
        SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.getDefault());
        return sdf.format(new Date(millis));
    }
}