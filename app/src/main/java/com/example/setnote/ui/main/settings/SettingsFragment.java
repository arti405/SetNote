package com.example.setnote.ui.main.settings;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.setnote.MainActivity;
import com.example.setnote.R;

public class SettingsFragment extends Fragment {

    public SettingsFragment() {
        super(R.layout.fragment_settings);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        ((MainActivity) requireActivity()).setToolbarTitle("Settings");
        View rowTheme = view.findViewById(R.id.rowTheme);
        rowTheme.setOnClickListener(v -> showThemeDialog());
        View rowDeleteAllData = view.findViewById(R.id.rowDeleteAllData);
        rowDeleteAllData.setOnClickListener(v -> showDeleteAllDataDialog());
        View rowDefaultSessionName = view.findViewById(R.id.rowDefaultSessionName);
        rowDefaultSessionName.setOnClickListener(v -> showDefaultSessionNameDialog());
        View rowDateFormat = view.findViewById(R.id.rowDateFormat);
        rowDateFormat.setOnClickListener(v -> showDateFormatDialog());
        View rowExportData = view.findViewById(R.id.rowExportData);
        rowExportData.setOnClickListener(v -> exportData());

        bindRow(view.findViewById(R.id.rowTheme),
                "Theme",
                "Choose light, dark or system mode",
                getThemeLabel());

        bindRow(view.findViewById(R.id.rowDefaultSessionName),
                "Default Session Name",
                "Set the default title for new sessions",
                getDefaultSessionNameLabel());

        bindRow(view.findViewById(R.id.rowDateFormat),
                "Date Format",
                "Choose how dates are displayed",
                getDateFormatLabel());

        bindRow(view.findViewById(R.id.rowExportData),
                "Export Data",
                "Export your app data",
                "");

        bindRow(view.findViewById(R.id.rowDeleteAllData),
                "Delete All Data",
                "Remove all sessions, templates and collections",
                "");

        bindRow(view.findViewById(R.id.rowVersion),
                "Version",
                "Current app version",
                "0.3");
    }

    private void exportData() {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Export Data")
                .setMessage("Create a full JSON export of all local app data?")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Export", (dialog, which) -> {
                    com.example.setnote.data.DbExecutors.IO.execute(() -> {
                        try {
                            java.io.File file = com.example.setnote.util.ExportHelper.exportAllDataToJson(requireContext());

                            requireActivity().runOnUiThread(() ->
                                    android.widget.Toast.makeText(
                                            requireContext(),
                                            "Export saved: " + file.getName(),
                                            android.widget.Toast.LENGTH_LONG
                                    ).show()
                            );
                        } catch (Exception e) {
                            e.printStackTrace();
                            requireActivity().runOnUiThread(() ->
                                    android.widget.Toast.makeText(
                                            requireContext(),
                                            "Export failed: " + e.getMessage(),
                                            android.widget.Toast.LENGTH_LONG
                                    ).show()
                            );
                        }
                    });
                })
                .show();
    }

    private String getDateFormatLabel() {
        return com.example.setnote.util.DateFormatHelper.getSavedFormat(requireContext());
    }

    private void showDateFormatDialog() {
        String[] labels = {
                "dd.MM.yyyy",
                "yyyy-MM-dd",
                "MMM dd, yyyy"
        };

        String[] values = {
                com.example.setnote.util.DateFormatHelper.FORMAT_1,
                com.example.setnote.util.DateFormatHelper.FORMAT_2,
                com.example.setnote.util.DateFormatHelper.FORMAT_3
        };

        String current = com.example.setnote.util.DateFormatHelper.getSavedFormat(requireContext());

        int checked = 0;
        for (int i = 0; i < values.length; i++) {
            if (values[i].equals(current)) {
                checked = i;
                break;
            }
        }

        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Date Format")
                .setSingleChoiceItems(labels, checked, (dialog, which) -> {
                    com.example.setnote.util.DateFormatHelper.saveFormat(requireContext(), values[which]);
                    dialog.dismiss();
                    refreshDateFormatRow();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void refreshDateFormatRow() {
        View row = requireView().findViewById(R.id.rowDateFormat);
        TextView tvValue = row.findViewById(R.id.tvValue);
        tvValue.setText(getDateFormatLabel());
    }
    private String getDefaultSessionNameLabel() {
        return com.example.setnote.util.SessionPrefsHelper.getDefaultSessionName(requireContext());
    }

    private void showDefaultSessionNameDialog() {
        android.widget.EditText input = new android.widget.EditText(requireContext());
        input.setText(getDefaultSessionNameLabel());
        input.setSelection(input.getText().length());
        input.setHint("Session");

        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Default Session Name")
                .setView(input)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Save", (dialog, which) -> {
                    String value = input.getText().toString().trim();
                    com.example.setnote.util.SessionPrefsHelper.saveDefaultSessionName(requireContext(), value);
                    refreshDefaultSessionNameRow();
                })
                .show();
    }

    private void refreshDefaultSessionNameRow() {
        View row = requireView().findViewById(R.id.rowDefaultSessionName);
        TextView tvValue = row.findViewById(R.id.tvValue);
        tvValue.setVisibility(View.VISIBLE);
        tvValue.setText(getDefaultSessionNameLabel());
    }

    private void bindRow(View row, String title, String subtitle, String value) {

        TextView tvTitle = row.findViewById(R.id.tvTitle);
        TextView tvSubtitle = row.findViewById(R.id.tvSubtitle);
        TextView tvValue = row.findViewById(R.id.tvValue);
        ImageView ivChevron = row.findViewById(R.id.ivChevron);

        tvTitle.setText(title);
        tvSubtitle.setText(subtitle);

        if (value == null || value.isEmpty()) {
            tvValue.setVisibility(View.GONE);
        } else {
            tvValue.setVisibility(View.VISIBLE);
            tvValue.setText(value);
        }

        // Version should not look like a navigable action
        if ("Version".equals(title)) {
            ivChevron.setVisibility(View.GONE);
        } else {
            ivChevron.setVisibility(View.VISIBLE);
        }

    }

    private void showThemeDialog() {
        String[] labels = {"System", "Light", "Dark"};
        String[] values = {
                com.example.setnote.util.ThemeHelper.THEME_SYSTEM,
                com.example.setnote.util.ThemeHelper.THEME_LIGHT,
                com.example.setnote.util.ThemeHelper.THEME_DARK
        };

        String current = com.example.setnote.util.ThemeHelper.getSavedTheme(requireContext());

        int checked = 0;
        for (int i = 0; i < values.length; i++) {
            if (values[i].equals(current)) {
                checked = i;
                break;
            }
        }

        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Choose Theme")
                .setSingleChoiceItems(labels, checked, (dialog, which) -> {
                    com.example.setnote.util.ThemeHelper.saveTheme(requireContext(), values[which]);
                    dialog.dismiss();
                    refreshThemeRow();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private String getThemeLabel() {
        String value = com.example.setnote.util.ThemeHelper.getSavedTheme(requireContext());

        if (com.example.setnote.util.ThemeHelper.THEME_LIGHT.equals(value)) {
            return "Light";
        }
        if (com.example.setnote.util.ThemeHelper.THEME_DARK.equals(value)) {
            return "Dark";
        }
        return "System";
    }

    private void refreshThemeRow() {
        View row = requireView().findViewById(R.id.rowTheme);
        TextView tvValue = row.findViewById(R.id.tvValue);
        tvValue.setVisibility(View.VISIBLE);
        tvValue.setText(getThemeLabel());
    }
    private void showDeleteAllDataDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Delete All Data?")
                .setMessage("This will remove all sessions, templates, collections, and archive data from this device.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Continue", (d, w) -> showFinalDeleteConfirmation())
                .show();
    }
    private void showFinalDeleteConfirmation() {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Final Warning")
                .setMessage("This action cannot be undone.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete Everything", (d, w) -> deleteAllData())
                .show();
    }
    private void deleteAllData() {
        com.example.setnote.data.DbExecutors.IO.execute(() -> {
            com.example.setnote.data.GymDao dao =
                    com.example.setnote.data.AppDatabase.getInstance(requireContext()).gymDao();

            // erst abhängige Tabellen, dann Haupttabellen
            dao.deleteAllCollectionSessions();
            dao.deleteAllCollections();

            dao.deleteAllTemplateSets();
            dao.deleteAllTemplateExercises();
            dao.deleteAllTemplates();

            dao.deleteAllSets();
            dao.deleteAllExercises();
            dao.deleteAllSessions();

            dao.deleteAllFolders();

            requireActivity().runOnUiThread(() -> {
                android.widget.Toast.makeText(requireContext(), "All data deleted", android.widget.Toast.LENGTH_SHORT).show();

                // zurück zum Hub
                requireActivity().getSupportFragmentManager().popBackStackImmediate(
                        null,
                        androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE
                );

                ((com.example.setnote.MainActivity) requireActivity()).setToolbarTitle("SetNote");
            });
        });

    }
}