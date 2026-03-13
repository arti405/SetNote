package com.example.tabnotes;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.tabnotes.data.AppDatabase;
import com.example.tabnotes.data.DbExecutors;
import com.example.tabnotes.data.FolderEntity;
import com.example.tabnotes.data.GymDao;
import com.example.tabnotes.data.SessionEntity;
import com.example.tabnotes.data.TemplateEntity;
import com.example.tabnotes.ui.main.ArchiveFragment;
import com.example.tabnotes.ui.main.HomeFragment;
import com.example.tabnotes.ui.main.SettingsFragment;
import com.example.tabnotes.ui.sessions.EditorActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.widget.Toolbar;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private int selectedTabId = R.id.nav_home;
    public void openAllSessions() {
        openFragment(new com.example.tabnotes.ui.main.SessionsFragment(), true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);

        if (savedInstanceState != null) {
            selectedTabId = savedInstanceState.getInt("selectedTabId", R.id.nav_home);
        }

        // initial fragment
        bottomNav.setSelectedItemId(selectedTabId);
        switchTo(selectedTabId);

        bottomNav.setOnItemSelectedListener(item -> {
            selectedTabId = item.getItemId();
            switchTo(selectedTabId);
            return true;
        });

        getSupportFragmentManager().addOnBackStackChangedListener(this::updateToolbarForBackstack);

        toolbar.setNavigationOnClickListener(v -> {
            // Back-Arrow klick = Back
            getOnBackPressedDispatcher().onBackPressed();
        });

// initial state
        updateToolbarForBackstack();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("selectedTabId", selectedTabId);
    }

    private void switchTo(int itemId) {
        clearFragmentBackStack();

        androidx.fragment.app.Fragment f;

        if (itemId == R.id.nav_archive) {
            f = new com.example.tabnotes.ui.main.archive.ArchiveRootFragment();
        } else if (itemId == R.id.nav_collections) {
            f = new com.example.tabnotes.ui.main.collections.CollectionsFragment();
        } else if (itemId == R.id.nav_settings) {
            f = new com.example.tabnotes.ui.main.SettingsFragment();
        } else {
            f = new com.example.tabnotes.ui.main.HomeFragment();
        }

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, f)
                .commit();

        // WICHTIG: Toolbar direkt auf Root-Tab zurücksetzen
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setTitle(titleForTab(itemId));
        }
    }
    private void clearFragmentBackStack() {
        getSupportFragmentManager().popBackStackImmediate(
                null,
                androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE
        );
    }
    private void updateToolbarForBackstack() {

        if (getSupportActionBar() == null) return;
        boolean canGoBack = getSupportFragmentManager().getBackStackEntryCount() > 0;
        getSupportActionBar().setDisplayHomeAsUpEnabled(canGoBack);

        if (!canGoBack) {
            getSupportActionBar().setTitle(titleForTab(selectedTabId));
        }
    }

    private String titleForTab(int tabId) {
        if (tabId == R.id.nav_archive) return "Archive";
        if (tabId == R.id.nav_collections) return "Collections";
        if (tabId == R.id.nav_settings) return "Settings";
        return "Home";
    }


    private void createEmptySessionAndOpenEditor() {
        AppDatabase db = AppDatabase.getInstance(this);

        DbExecutors.IO.execute(() -> {
            SessionEntity s = new SessionEntity();
            s.title = "New Session";
            s.dateEpochMillis = System.currentTimeMillis();
            long id = db.gymDao().insertSession(s);

            runOnUiThread(() -> {
                Intent i = new Intent(this, EditorActivity.class);
                i.putExtra("sessionId", id);
                startActivity(i);
            });
        });
    }

    public void createSessionFromTemplate(long templateId) {
        AppDatabase db = AppDatabase.getInstance(this);

        DbExecutors.IO.execute(() -> {
            try {
                // 1) neue Session anlegen
                SessionEntity s = new SessionEntity();
                s.title = "New Session";
                s.dateEpochMillis = System.currentTimeMillis();
                long sessionId = db.gymDao().insertSession(s);

                // 2) Template Exercises lesen
                java.util.List<com.example.tabnotes.data.TemplateExerciseEntity> tEx =
                        db.gymDao().getTemplateExercises(templateId);

                for (int i = 0; i < tEx.size(); i++) {
                    com.example.tabnotes.data.TemplateExerciseEntity te = tEx.get(i);

                    // Exercise kopieren
                    com.example.tabnotes.data.ExerciseEntity ex = new com.example.tabnotes.data.ExerciseEntity();
                    ex.sessionId = sessionId;
                    ex.name = te.name;
                    ex.position = i;

                    long exId = db.gymDao().insertExercise(ex);

                    // Sets kopieren
                    java.util.List<com.example.tabnotes.data.TemplateSetEntity> tSets =
                            db.gymDao().getTemplateSets(te.id);

                    java.util.ArrayList<com.example.tabnotes.data.SetEntity> sets = new java.util.ArrayList<>();
                    for (int j = 0; j < tSets.size(); j++) {
                        com.example.tabnotes.data.TemplateSetEntity ts = tSets.get(j);

                        com.example.tabnotes.data.SetEntity se = new com.example.tabnotes.data.SetEntity();
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
            s.title = "New Session";
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
}
