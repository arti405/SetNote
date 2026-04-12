package com.arti405.setnote.util;

import android.content.Context;

import com.arti405.setnote.data.AppDatabase;
import com.arti405.setnote.data.GymDao;
import com.arti405.setnote.data.export.ExportBundle;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ExportHelper {

    public static File exportAllDataToJson(Context context) throws Exception {
        GymDao dao = AppDatabase.getInstance(context).gymDao();

        ExportBundle bundle = new ExportBundle();
        bundle.exportedAt = System.currentTimeMillis();

        bundle.sessions = dao.getAllSessionsNow();
        bundle.exercises = dao.getAllExercisesNow();
        bundle.sets = dao.getAllSetsNow();

        bundle.templates = dao.getAllTemplatesNow();
        bundle.templateExercises = dao.getAllTemplateExercisesNow();
        bundle.templateSets = dao.getAllTemplateSetsNow();

        bundle.folders = dao.getAllFoldersNow();

        bundle.collections = dao.getAllCollectionsNow();
        bundle.collectionSessions = dao.getAllCollectionSessionsNow();

        JSONObject root = new JSONObject();
        root.put("exportedAt", bundle.exportedAt);
        root.put("sessions", new JSONArray(new com.google.gson.Gson().toJson(bundle.sessions)));
        root.put("exercises", new JSONArray(new com.google.gson.Gson().toJson(bundle.exercises)));
        root.put("sets", new JSONArray(new com.google.gson.Gson().toJson(bundle.sets)));

        root.put("templates", new JSONArray(new com.google.gson.Gson().toJson(bundle.templates)));
        root.put("templateExercises", new JSONArray(new com.google.gson.Gson().toJson(bundle.templateExercises)));
        root.put("templateSets", new JSONArray(new com.google.gson.Gson().toJson(bundle.templateSets)));

        root.put("folders", new JSONArray(new com.google.gson.Gson().toJson(bundle.folders)));

        root.put("collections", new JSONArray(new com.google.gson.Gson().toJson(bundle.collections)));
        root.put("collectionSessions", new JSONArray(new com.google.gson.Gson().toJson(bundle.collectionSessions)));

        File exportDir = new File(context.getFilesDir(), "exports");
        if (!exportDir.exists()) exportDir.mkdirs();

        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File outFile = new File(exportDir, "setnote_export_" + timestamp + ".json");

        try (FileOutputStream fos = new FileOutputStream(outFile)) {
            fos.write(root.toString(2).getBytes(StandardCharsets.UTF_8));
            fos.flush();
        }

        return outFile;
    }
}
