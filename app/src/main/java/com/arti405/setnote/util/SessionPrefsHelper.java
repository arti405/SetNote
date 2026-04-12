package com.arti405.setnote.util;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionPrefsHelper {

    private static final String PREFS_NAME = "setnote_prefs";
    private static final String KEY_DEFAULT_SESSION_NAME = "default_session_name";

    public static String getDefaultSessionName(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_DEFAULT_SESSION_NAME, "Session");
    }

    public static void saveDefaultSessionName(Context context, String value) {
        if (value == null || value.trim().isEmpty()) {
            value = "Session";
        }

        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_DEFAULT_SESSION_NAME, value.trim()).apply();
    }
}
