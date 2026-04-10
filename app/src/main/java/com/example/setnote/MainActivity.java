package com.example.setnote;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.setnote.data.AppDatabase;
import com.example.setnote.data.DbExecutors;
import com.example.setnote.data.FolderEntity;
import com.example.setnote.data.GymDao;
import com.example.setnote.data.SessionEntity;
import com.example.setnote.data.TemplateEntity;
import com.example.setnote.ui.sessions.EditorActivity;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    public void openAllSessions() {
        openFragment(new com.example.setnote.ui.main.SessionsFragment(), true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        com.example.setnote.util.ThemeHelper.applySavedTheme(this);
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
                    .replace(R.id.fragmentContainer, new com.example.setnote.ui.main.HubFragment())
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
                s.title = com.example.setnote.util.SessionPrefsHelper.getDefaultSessionName(this);
                s.dateEpochMillis = System.currentTimeMillis();
                long sessionId = db.gymDao().insertSession(s);

                // 2) Template Exercises lesen
                java.util.List<com.example.setnote.data.TemplateExerciseEntity> tEx =
                        db.gymDao().getTemplateExercises(templateId);

                for (int i = 0; i < tEx.size(); i++) {
                    com.example.setnote.data.TemplateExerciseEntity te = tEx.get(i);

                    // Exercise kopieren
                    com.example.setnote.data.ExerciseEntity ex = new com.example.setnote.data.ExerciseEntity();
                    ex.sessionId = sessionId;
                    ex.name = te.name;
                    ex.position = i;

                    long exId = db.gymDao().insertExercise(ex);

                    // Sets kopieren
                    java.util.List<com.example.setnote.data.TemplateSetEntity> tSets =
                            db.gymDao().getTemplateSets(te.id);

                    java.util.ArrayList<com.example.setnote.data.SetEntity> sets = new java.util.ArrayList<>();
                    for (int j = 0; j < tSets.size(); j++) {
                        com.example.setnote.data.TemplateSetEntity ts = tSets.get(j);

                        com.example.setnote.data.SetEntity se = new com.example.setnote.data.SetEntity();
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
            s.title = com.example.setnote.util.SessionPrefsHelper.getDefaultSessionName(this);
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
            openFragment(new com.example.setnote.ui.main.settings.SettingsFragment(), true);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
