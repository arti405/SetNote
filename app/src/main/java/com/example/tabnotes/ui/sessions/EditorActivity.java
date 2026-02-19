package com.example.tabnotes.ui.sessions;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tabnotes.R;
import com.example.tabnotes.data.AppDatabase;
import com.example.tabnotes.data.DbExecutors;
import com.example.tabnotes.data.ExerciseEntity;
import com.example.tabnotes.data.GymDao;
import com.example.tabnotes.data.SessionEntity;
import com.example.tabnotes.data.SetEntity;
import com.example.tabnotes.ui.sessions.editor.ExerciseAdapter;
import com.example.tabnotes.ui.sessions.editor.ExerciseBlock;

import java.util.ArrayList;

public class EditorActivity extends AppCompatActivity {

    private final ArrayList<ExerciseBlock> blocks = new ArrayList<>();
    private ExerciseAdapter adapter;

    private int position = -1;
    private String sessionDate = "";
    private String sessionTitle = "Session";

    private TextView tvHeader;
    private long sessionId = -1;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        sessionId = getIntent().getLongExtra("sessionId", -1);
        if (sessionId >= 0) loadFromDb(sessionId);
        // Intent extras einmal lesen
        position = getIntent().getIntExtra("position", -1);

        String t = getIntent().getStringExtra("title");
        String d = getIntent().getStringExtra("date");

        if (t != null && !t.trim().isEmpty()) sessionTitle = t.trim();
        if (d != null) sessionDate = d;

        tvHeader = findViewById(R.id.tvHeader);
        updateHeader();

        // Recycler
        RecyclerView rv = findViewById(R.id.rvRows);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ExerciseAdapter(blocks);
        rv.setAdapter(adapter);

        // Start: 1 Block
        blocks.add(new ExerciseBlock());
        adapter.notifyItemInserted(0);

        Button btnAdd = findViewById(R.id.btnAddRow);
        btnAdd.setText("Add Exercise");
        btnAdd.setOnClickListener(v -> {
            blocks.add(new ExerciseBlock());
            adapter.notifyItemInserted(blocks.size() - 1);
            rv.scrollToPosition(blocks.size() - 1);
        });

        // Rename nur über Icon
        ImageButton btnEditTitle = findViewById(R.id.btnEditTitle);
        btnEditTitle.setOnClickListener(v -> showRenameDialog());

        // Back Gesture / Back Button
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (sessionId >= 0) saveToDb(sessionId);
                finish();
            }
        });

        if (sessionId >= 0) {
            loadFromDb(sessionId);
        }
        Button btnExport = findViewById(R.id.btnExportPdf);
        btnExport.setOnClickListener(v -> exportPdf());

    }
    private void exportPdf() {
        // Export im IO-Thread, sonst UI freeze
        DbExecutors.IO.execute(() -> {
            try {
                // Optional: erst speichern, damit DB aktuell ist
                if (sessionId >= 0) saveToDb(sessionId);

                java.io.File pdf = com.example.tabnotes.export.PdfExporter.exportSessionToPdf(
                        this,
                        sessionTitle,
                        sessionDate,
                        blocks
                );

                android.net.Uri uri = androidx.core.content.FileProvider.getUriForFile(
                        this,
                        getPackageName() + ".fileprovider",
                        pdf
                );

                runOnUiThread(() -> {
                    Intent share = new Intent(Intent.ACTION_SEND);
                    share.setType("application/pdf");
                    share.putExtra(Intent.EXTRA_STREAM, uri);
                    share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivity(Intent.createChooser(share, "Share PDF"));
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() ->
                        android.widget.Toast.makeText(this, "PDF export failed: " + e.getMessage(), android.widget.Toast.LENGTH_LONG).show()
                );
            }
        });
    }


    private void updateHeader() {
        tvHeader.setText(sessionTitle + " • " + sessionDate);
    }

    private void showRenameDialog() {
        EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        input.setText(sessionTitle);
        input.setSelection(input.getText().length());

        int pad = (int) (16 * getResources().getDisplayMetrics().density);
        input.setPadding(pad, pad, pad, pad);

        new AlertDialog.Builder(this)
                .setTitle("Rename session")
                .setView(input)
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .setPositiveButton("Save", (dialog, which) -> {
                    String newTitle = input.getText().toString().trim();
                    if (newTitle.isEmpty()) newTitle = "Session";

                    // Wichtig: Erst updaten, dann Result setzen
                    sessionTitle = newTitle;
                    updateHeader();
                    if (sessionId >= 0) saveToDb(sessionId);
                    // Result schon setzen (ohne zu schließen)
                    sendResultOnly();
                })
                .show();
    }

    private void sendResultOnly() {
        if (position < 0) return;
        Intent resultIntent = new Intent();
        resultIntent.putExtra("position", position);
        resultIntent.putExtra("newTitle", sessionTitle);
        setResult(RESULT_OK, resultIntent);
    }

    private void sendResultAndFinish() {
        sendResultOnly();
        finish();
    }

    private void loadFromDb(long sessionId) {
        DbExecutors.IO.execute(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            GymDao dao = db.gymDao();

            SessionEntity s = dao.getSessionById(sessionId);
            if (s != null) {
                sessionTitle = (s.title == null || s.title.trim().isEmpty()) ? "Session" : s.title.trim();
                sessionDate = android.text.format.DateFormat.format("yyyy-MM-dd", s.dateEpochMillis).toString();
            }

            // UI-Model bauen
            ArrayList<ExerciseBlock> loaded = new ArrayList<>();

            for (ExerciseEntity ex : dao.getExercisesForSession(sessionId)) {
                ExerciseBlock block = new ExerciseBlock();
                block.exercise = ex.name == null ? "" : ex.name;

                // vorhandene Default-Set-Liste überschreiben
                block.sets.clear();

                for (SetEntity se : dao.getSetsForExercise(ex.id)) {
                    com.example.tabnotes.ui.sessions.editor.SetRow sr = new com.example.tabnotes.ui.sessions.editor.SetRow();
                    sr.weight = se.weight == null ? "" : se.weight;
                    sr.reps = se.reps == null ? "" : se.reps;
                    sr.note = se.note == null ? "" : se.note;
                    block.sets.add(sr);
                }

                if (block.sets.isEmpty()) block.sets.add(new com.example.tabnotes.ui.sessions.editor.SetRow());
                loaded.add(block);
            }

            if (loaded.isEmpty()) loaded.add(new ExerciseBlock());

            runOnUiThread(() -> {
                blocks.clear();
                blocks.addAll(loaded);
                adapter.notifyDataSetChanged();
                updateHeader();
            });
        });
    }

    private void saveToDb(long sessionId) {
        DbExecutors.IO.execute(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            GymDao dao = db.gymDao();

            // Session updaten (Titel)
            SessionEntity s = dao.getSessionById(sessionId);
            if (s != null) {
                s.title = sessionTitle;
                dao.updateSession(s);
            }

            // Alles neu schreiben (Phase 1: simpel)
            dao.deleteSetsForSession(sessionId);
            dao.deleteExercisesForSession(sessionId);

            for (int i = 0; i < blocks.size(); i++) {
                ExerciseBlock b = blocks.get(i);

                ExerciseEntity ex = new ExerciseEntity();
                ex.sessionId = sessionId;
                ex.name = b.exercise;
                ex.position = i;

                long exId = dao.insertExercise(ex);

                // Sets
                java.util.ArrayList<SetEntity> setEntities = new java.util.ArrayList<>();
                for (int j = 0; j < b.sets.size(); j++) {
                    com.example.tabnotes.ui.sessions.editor.SetRow sr = b.sets.get(j);

                    SetEntity se = new SetEntity();
                    se.exerciseId = exId;
                    se.weight = sr.weight;
                    se.reps = sr.reps;
                    se.note = sr.note;
                    se.position = j;
                    setEntities.add(se);
                }
                dao.insertSets(setEntities);
            }
        });
    }


}


