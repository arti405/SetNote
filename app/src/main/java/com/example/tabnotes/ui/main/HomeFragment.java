package com.example.tabnotes.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tabnotes.R;
import com.example.tabnotes.data.AppDatabase;
import com.example.tabnotes.data.SessionEntity;
import com.example.tabnotes.ui.sessions.EditorActivity;
import com.example.tabnotes.ui.sessions.SessionEntityAdapter;

import java.util.ArrayList;

public class HomeFragment extends Fragment {

    private final ArrayList<SessionEntity> recent = new ArrayList<>();
    private SessionEntityAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        RecyclerView rv = view.findViewById(R.id.rvRecent);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new SessionEntityAdapter(
                recent,
                session -> {
                    Intent i = new Intent(requireContext(), EditorActivity.class);
                    i.putExtra("sessionId", session.id);
                    startActivity(i);
                },
                this::showSessionMenu
        );
        rv.setAdapter(adapter);

        view.findViewById(R.id.btnHomePlus).setOnClickListener(v -> showHomeCreateDialog());

        AppDatabase db = AppDatabase.getInstance(requireContext());
        db.gymDao().observeAllSessions().observe(getViewLifecycleOwner(), list -> {
            recent.clear();
            if (list != null) recent.addAll(list);
            adapter.notifyDataSetChanged();
        });
    }

    private void showHomeCreateDialog() {
        String[] options = {"New Session", "Start from Template"};

        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Create")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        ((com.example.tabnotes.MainActivity) requireActivity()).createSessionAutoFolderAndOpenEditor();
                    } else if (which == 1) {
                        ((com.example.tabnotes.MainActivity) requireActivity()).showTemplatePicker();
                    }
                })
                .show();
    }
    private void showSessionMenu(View anchor, com.example.tabnotes.data.SessionEntity session) {
        androidx.appcompat.widget.PopupMenu popup = new androidx.appcompat.widget.PopupMenu(requireContext(), anchor);
        popup.getMenu().add("Rename");
        popup.getMenu().add("Delete");

        popup.setOnMenuItemClickListener(item -> {
            String title = item.getTitle().toString();

            if ("Rename".equals(title)) {
                showRenameDialog(session);
                return true;
            }

            if ("Delete".equals(title)) {
                showDeleteDialog(session);
                return true;
            }

            return false;
        });

        popup.show();
    }

    private void showRenameDialog(com.example.tabnotes.data.SessionEntity session) {
        android.widget.EditText input = new android.widget.EditText(requireContext());
        input.setText(session.title == null ? "" : session.title);
        input.setSelection(input.getText().length());

        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Rename Session")
                .setView(input)
                .setNegativeButton("Cancel", (d, w) -> d.dismiss())
                .setPositiveButton("Save", (d, w) -> {
                    String newTitle = input.getText().toString().trim();
                    if (newTitle.isEmpty()) newTitle = "Session";

                    String finalTitle = newTitle;
                    com.example.tabnotes.data.DbExecutors.IO.execute(() ->
                            com.example.tabnotes.data.AppDatabase
                                    .getInstance(requireContext())
                                    .gymDao()
                                    .renameSession(session.id, finalTitle)
                    );
                })
                .show();
    }

    private void showDeleteDialog(com.example.tabnotes.data.SessionEntity session) {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Delete Session?")
                .setMessage("This deletes the session permanently.")
                .setNegativeButton("Cancel", (d, w) -> d.dismiss())
                .setPositiveButton("Delete", (d, w) -> {
                    com.example.tabnotes.data.DbExecutors.IO.execute(() ->
                            com.example.tabnotes.data.AppDatabase
                                    .getInstance(requireContext())
                                    .gymDao()
                                    .deleteSession(session.id)
                    );
                })
                .show();
    }

}