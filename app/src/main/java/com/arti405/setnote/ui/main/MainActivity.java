package com.arti405.setnote.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.arti405.setnote.R;
import com.arti405.setnote.data.AppDatabase;
import com.arti405.setnote.data.DbExecutors;
import com.arti405.setnote.data.ExerciseEntity;
import com.arti405.setnote.data.FolderEntity;
import com.arti405.setnote.data.GymDao;
import com.arti405.setnote.data.SessionEntity;
import com.arti405.setnote.data.TemplateEntity;
import com.arti405.setnote.ui.editor.EditorActivity;

import java.util.List;

import com.arti405.setnote.data.SetEntity;
import com.arti405.setnote.data.TemplateExerciseEntity;
import com.arti405.setnote.data.TemplateSetEntity;
import com.arti405.setnote.ui.main.settings.SettingsFragment;
import com.arti405.setnote.util.SessionPrefsHelper;
import com.arti405.setnote.util.ThemeHelper;

public class MainActivity extends AppCompatActivity {

    public void openAllSessions() {
        openFragment(new SessionsFragment(), true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.applySavedTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportFragmentManager().addOnBackStackChangedListener(this::updateToolbarForBackstack);

        toolbar.setNavigationOnClickListener(v ->
                getOnBackPressedDispatcher().onBackPressed()
        );

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, new HubFragment())
                    .commit();
        }

        updateToolbarForBackstack();
    }
    private void updateToolbarForBackstack() {
        if (getSupportActionBar() == null) return;

        boolean canGoBack = getSupportFragmentManager().getBackStackEntryCount() > 0;
        getSupportActionBar().setDisplayHomeAsUpEnabled(canGoBack);

        if (!canGoBack) {
            getSupportActionBar().setTitle("SetNote");
        }
    }



    public void createSessionFromTemplate(long templateId) {
        AppDatabase db = AppDatabase.getInstance(this);

        DbExecutors.IO.execute(() -> {
            try {
                // 1) neue Session anlegen
                SessionEntity s = new SessionEntity();
                s.title = SessionPrefsHelper.getDefaultSessionName(this);
                s.dateEpochMillis = System.currentTimeMillis();
                long sessionId = db.gymDao().insertSession(s);

                // 2) Template Exercises lesen
                java.util.List<TemplateExerciseEntity> tEx =
                        db.gymDao().getTemplateExercises(templateId);

                for (int i = 0; i < tEx.size(); i++) {
                    TemplateExerciseEntity te = tEx.get(i);

                    // Exercise kopieren
                    ExerciseEntity ex = new ExerciseEntity();
                    ex.sessionId = sessionId;
                    ex.name = te.name;
                    ex.position = i;

                    long exId = db.gymDao().insertExercise(ex);

                    // Sets kopieren
                    java.util.List<TemplateSetEntity> tSets =
                            db.gymDao().getTemplateSets(te.id);

                    java.util.ArrayList<SetEntity> sets = new java.util.ArrayList<>();
                    for (int j = 0; j < tSets.size(); j++) {
                        TemplateSetEntity ts = tSets.get(j);

                        SetEntity se = new SetEntity();
                        se.exerciseId = exId;
                        se.weight = ts.weight;
                        se.reps = ts.reps;
                        se.note = ts.note;
                        se.position = j;

                        sets.add(se);
                    }
                    if (!sets.isEmpty()) db.gymDao().insertSets(sets);
                }

                // 3) Editor öffnen
                runOnUiThread(() -> {
                    Intent i = new Intent(this, EditorActivity.class);
                    i.putExtra("sessionId", sessionId);
                    startActivity(i);
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() ->
                        android.widget.Toast.makeText(this, "Template import failed: " + e.getMessage(),
                                android.widget.Toast.LENGTH_LONG).show()
                );
            }
        });
    }

    public void showTemplatePicker() {
        AppDatabase db = AppDatabase.getInstance(this);

        DbExecutors.IO.execute(() -> {
            List<TemplateEntity> templates = db.gymDao().getTemplates(); // NON-LiveData Variante

            runOnUiThread(() -> {
                if (templates == null || templates.isEmpty()) {
                    Toast.makeText(this, "No templates yet. Save one from a session first.", Toast.LENGTH_LONG).show();
                    return;
                }

                String[] names = new String[templates.size()];
                for (int i = 0; i < templates.size(); i++) {
                    String n = templates.get(i).name;
                    names[i] = (n == null || n.trim().isEmpty()) ? ("Template " + templates.get(i).id) : n;
                }

                new AlertDialog.Builder(this)
                        .setTitle("Choose template")
                        .setItems(names, (d, which) -> {
                            long templateId = templates.get(which).id;
                            createSessionFromTemplate(templateId);
                        })
                        .setNegativeButton("Cancel", (d, w) -> d.dismiss())
                        .show();
            });
        });
    }


    public void createSessionAutoFolderAndOpenEditor() {
        AppDatabase db = AppDatabase.getInstance(this);

        DbExecutors.IO.execute(() -> {
            GymDao dao = db.gymDao();

            java.util.Calendar c = java.util.Calendar.getInstance();
            int year = c.get(java.util.Calendar.YEAR);
            int month = c.get(java.util.Calendar.MONTH) + 1;

            FolderEntity yearFolder = dao.getYearFolder(year);
            if (yearFolder == null) {
                FolderEntity yf = new FolderEntity();
                yf.type = "YEAR";
                yf.year = year;
                yf.parentId = null;
                yf.name = String.valueOf(year);
                yf.createdAt = System.currentTimeMillis();
                long id = dao.insertFolder(yf);
                yf.id = id;
                yearFolder = yf;
            }

            FolderEntity monthFolder = dao.getMonthFolder(year, month);
            if (monthFolder == null) {
                FolderEntity mf = new FolderEntity();
                mf.type = "MONTH";
                mf.year = year;
                mf.month = month;
                mf.parentId = yearFolder.id;
                mf.name = monthName(month);
                mf.createdAt = System.currentTimeMillis();
                long id = dao.insertFolder(mf);
                mf.id = id;
                monthFolder = mf;
            }

            SessionEntity s = new SessionEntity();
            s.title = SessionPrefsHelper.getDefaultSessionName(this);
            s.dateEpochMillis = System.currentTimeMillis();
            s.folderId = monthFolder.id;

            long sessionId = dao.insertSession(s);

            runOnUiThread(() -> {
                Intent i = new Intent(this, EditorActivity.class);
                i.putExtra("sessionId", sessionId);
                startActivity(i);
            });
        });
    }

    private String monthName(int month) {
        String[] m = {"Januar","Februar","März","April","Mai","Juni","Juli","August","September","Oktober","November","Dezember"};
        return m[Math.max(1, Math.min(12, month)) - 1];
    }


    public void openFragment(androidx.fragment.app.Fragment fragment, boolean addToBackStack) {
        androidx.fragment.app.FragmentTransaction tx = getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment);

        if (addToBackStack) {
            tx.addToBackStack(null);
        }

        tx.commit();
    }


    public void setToolbarTitle(String title) {
        if (getSupportActionBar() != null) getSupportActionBar().setTitle(title);
    }


    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            openFragment(new SettingsFragment(), true);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
