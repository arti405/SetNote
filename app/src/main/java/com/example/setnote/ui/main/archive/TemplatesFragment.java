package com.example.setnote.ui.main.archive;

import android.os.Bundle;
import android.view.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.setnote.MainActivity;
import com.example.setnote.R;
import com.example.setnote.data.AppDatabase;
import com.example.setnote.data.TemplateEntity;

import java.util.ArrayList;

public class TemplatesFragment extends Fragment {

    private final ArrayList<TemplateEntity> templates = new ArrayList<>();
    private SimpleTemplateAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_simple_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        ((MainActivity) requireActivity()).setToolbarTitle("Templates");

        RecyclerView rv = view.findViewById(R.id.rv);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new SimpleTemplateAdapter(
                templates,
                t -> ((MainActivity) requireActivity()).createSessionFromTemplate(t.id),
                this::showTemplateMenu
        );

        rv.setAdapter(adapter);

        AppDatabase.getInstance(requireContext())
                .gymDao()
                .observeTemplates()
                .observe(getViewLifecycleOwner(), list -> {
                    templates.clear();
                    if (list != null) templates.addAll(list);
                    adapter.notifyDataSetChanged();
                });
    }
    private void showTemplateMenu(View anchor, com.example.setnote.data.TemplateEntity template) {
        androidx.appcompat.widget.PopupMenu popup =
                new androidx.appcompat.widget.PopupMenu(requireContext(), anchor);

        popup.getMenu().add("Rename");
        popup.getMenu().add("Delete");

        popup.setOnMenuItemClickListener(item -> {
            String title = item.getTitle().toString();

            if ("Rename".equals(title)) {
                showRenameDialog(template);
                return true;
            }

            if ("Delete".equals(title)) {
                showDeleteDialog(template);
                return true;
            }

            return false;
        });

        popup.show();
    }

    private void showRenameDialog(com.example.setnote.data.TemplateEntity template) {
        android.widget.EditText input = new android.widget.EditText(requireContext());
        input.setText(template.name == null ? "" : template.name);
        input.setSelection(input.getText().length());

        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Rename Template")
                .setView(input)
                .setNegativeButton("Cancel", (d, w) -> d.dismiss())
                .setPositiveButton("Save", (d, w) -> {
                    String newName = input.getText().toString().trim();
                    if (newName.isEmpty()) newName = "Template";

                    String finalName = newName;
                    com.example.setnote.data.DbExecutors.IO.execute(() ->
                            com.example.setnote.data.AppDatabase
                                    .getInstance(requireContext())
                                    .gymDao()
                                    .renameTemplate(template.id, finalName)
                    );
                })
                .show();
    }

    private void showDeleteDialog(com.example.setnote.data.TemplateEntity template) {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Delete Template?")
                .setMessage("This deletes the template permanently.")
                .setNegativeButton("Cancel", (d, w) -> d.dismiss())
                .setPositiveButton("Delete", (d, w) -> {
                    com.example.setnote.data.DbExecutors.IO.execute(() ->
                            com.example.setnote.data.AppDatabase
                                    .getInstance(requireContext())
                                    .gymDao()
                                    .deleteTemplate(template.id)
                    );
                })
                .show();
    }
}
