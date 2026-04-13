package com.arti405.setnote.ui.sessions;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.arti405.setnote.R;
import com.arti405.setnote.data.AppDatabase;
import com.arti405.setnote.data.DbExecutors;
import com.arti405.setnote.data.ExerciseEntity;
import com.arti405.setnote.data.GymDao;
import com.arti405.setnote.data.SessionEntity;
import com.arti405.setnote.data.SetEntity;
import com.arti405.setnote.data.TemplateEntity;
import com.arti405.setnote.data.TemplateExerciseEntity;
import com.arti405.setnote.data.TemplateSetEntity;
import com.arti405.setnote.data.export.PdfExporter;
import com.arti405.setnote.ui.editor.SetRow;
import com.arti405.setnote.ui.editor.ExerciseAdapter;
import com.arti405.setnote.ui.editor.ExerciseBlock;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.ArrayList;

public class EditorActivity extends AppCompatActivity {

    private final ArrayList<ExerciseBlock> blocks = new ArrayList<>();
    private ExerciseAdapter adapter;

    private String sessionDate = "";
    private String sessionTitle = "Session";

    private TextView tvHeaderTitle;
    private TextView tvHeaderDate;

    private long sessionId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Views
        tvHeaderTitle = findViewById(R.id.tvHeaderTitle);
        tvHeaderDate = findViewById(R.id.tvHeaderDate);
        Toolbar toolbar = findViewById(R.id.toolbar);

        toolbar.setNavigationOnClickListener(v ->
                getOnBackPressedDispatcher().onBackPressed()
        );

        toolbar.setOnMenuItemClickListener(item -> {
            String title = item.getTitle().toString();
            if ("Rename Session".equals(title)) {
                showRenameDialog();
                return true;
            }
            if ("Save as Template".equals(title)) {
                showSaveTemplateDialog();
                return true;
            }
            if ("Export PDF".equals(title)) {
                exportPdf();
                return true;
            }
            return false;
        });

        // Add menu manually since we aren't using setSupportActionBar for custom layout
        toolbar.getMenu().add("Rename Session");
        toolbar.getMenu().add("Save as Template");
        toolbar.getMenu().add("Export PDF");

        // Intent
        sessionId = getIntent().getLongExtra("sessionId", -1);

        String t = getIntent().getStringExtra("title");
        String d = getIntent().getStringExtra("date");

        if (t != null && !t.trim().isEmpty()) sessionTitle = t.trim();
        if (d != null) sessionDate = d;

        updateHeader();

        // Recycler
        RecyclerView rv = findViewById(R.id.rvRows);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ExerciseAdapter(blocks);
        rv.setAdapter(adapter);

        // Load DB
        if (sessionId >= 0) {
            loadFromDb(sessionId);
        } else {
            blocks.add(new ExerciseBlock());
            adapter.notifyItemInserted(0);
        }

        // Add Exercise
        ExtendedFloatingActionButton btnAdd = findViewById(R.id.btnAddRow);
        btnAdd.setOnClickListener(v -> {
            blocks.add(new ExerciseBlock());
            adapter.notifyItemInserted(blocks.size() - 1);
            rv.smoothScrollToPosition(blocks.size() - 1);
        });

        // Drag & Drop
        ItemTouchHelper helper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {

                int from = viewHolder.getBindingAdapterPosition();
                int to = target.getBindingAdapterPosition();

                java.util.Collections.swap(blocks, from, to);
                adapter.notifyItemMoved(from, to);
                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {}
        });
        helper.attachToRecyclerView(rv);

        // Back handling
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (sessionId >= 0) saveToDb(sessionId);
                finish();
            }
        });
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

                    sessionTitle = newTitle;
                    updateHeader();

                    if (sessionId >= 0) saveToDb(sessionId);
                })
                .show();
    }

    private void showSaveTemplateDialog() {
        EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        input.setText(sessionTitle);
        input.setSelection(input.getText().length());

        int pad = (int) (16 * getResources().getDisplayMetrics().density);
        input.setPadding(pad, pad, pad, pad);

        new AlertDialog.Builder(this)
                .setTitle("Template name")
                .setView(input)
                .setNegativeButton("Cancel", (d, w) -> d.dismiss())
                .setPositiveButton("Save", (d, w) -> {
                    String name = input.getText().toString().trim();
                    if (name.isEmpty()) name = "Template";
                    saveCurrentAsTemplate(name);
                })
                .show();
    }

    private void saveCurrentAsTemplate(String templateName) {
        DbExecutors.IO.execute(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            GymDao dao = db.gymDao();

            TemplateEntity t = new TemplateEntity();
            t.name = templateName;
            t.createdAt = System.currentTimeMillis();
            long templateId = dao.insertTemplate(t);

            for (int i = 0; i < blocks.size(); i++) {
                ExerciseBlock b = blocks.get(i);

                TemplateExerciseEntity te = new TemplateExerciseEntity();
                te.templateId = templateId;
                te.name = (b.exercise == null) ? "" : b.exercise;
                te.position = i;

                long teId = dao.insertTemplateExercise(te);

                java.util.ArrayList<TemplateSetEntity> templateSets = new java.util.ArrayList<>();
                for (int j = 0; j < b.sets.size(); j++) {
                    SetRow sr = b.sets.get(j);

                    TemplateSetEntity ts = new TemplateSetEntity();
                    ts.templateExerciseId = teId;
                    ts.weight = sr.weight;
                    ts.reps = sr.reps;
                    ts.note = sr.note;
                    ts.position = j;

                    templateSets.add(ts);
                }

                dao.insertTemplateSets(templateSets);
            }

            runOnUiThread(() ->
                    Toast.makeText(this, "Template saved", Toast.LENGTH_SHORT).show()
            );
        });
    }

    private void exportPdf() {
        DbExecutors.IO.execute(() -> {
            try {
                if (sessionId >= 0) saveToDb(sessionId);

                java.io.File pdf = PdfExporter.exportSessionToPdf(
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
                        Toast.makeText(this, "PDF export failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
            }
        });
    }

    private void updateHeader() {
        if (tvHeaderTitle != null) tvHeaderTitle.setText(sessionTitle);
        if (tvHeaderDate != null) tvHeaderDate.setText(sessionDate);
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

            ArrayList<ExerciseBlock> loaded = new ArrayList<>();

            for (ExerciseEntity ex : dao.getExercisesForSession(sessionId)) {
                ExerciseBlock block = new ExerciseBlock();
                block.exercise = ex.name == null ? "" : ex.name;
                block.sets.clear();

                for (SetEntity se : dao.getSetsForExercise(ex.id)) {
                    SetRow sr = new SetRow();
                    sr.weight = se.weight == null ? "" : se.weight;
                    sr.reps = se.reps == null ? "" : se.reps;
                    sr.note = se.note == null ? "" : se.note;
                    block.sets.add(sr);
                }

                if (block.sets.isEmpty()) block.sets.add(new SetRow());
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

            SessionEntity s = dao.getSessionById(sessionId);
            if (s != null) {
                s.title = sessionTitle;
                dao.updateSession(s);
            }

            dao.deleteSetsForSession(sessionId);
            dao.deleteExercisesForSession(sessionId);

            for (int i = 0; i < blocks.size(); i++) {
                ExerciseBlock b = blocks.get(i);

                ExerciseEntity ex = new ExerciseEntity();
                ex.sessionId = sessionId;
                ex.name = b.exercise;
                ex.position = i;

                long exId = dao.insertExercise(ex);

                ArrayList<SetEntity> setEntities = new ArrayList<>();
                for (int j = 0; j < b.sets.size(); j++) {
                    SetRow sr = b.sets.get(j);

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
