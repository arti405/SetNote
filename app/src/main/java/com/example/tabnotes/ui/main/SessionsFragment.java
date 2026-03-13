package com.example.tabnotes.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.*;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tabnotes.R;
import com.example.tabnotes.data.AppDatabase;
import com.example.tabnotes.data.DbExecutors;
import com.example.tabnotes.data.SessionEntity;
import com.example.tabnotes.ui.sessions.EditorActivity;
import com.example.tabnotes.ui.sessions.SessionEntityAdapter;
import android.content.Intent;
import android.view.View;

import java.util.ArrayList;

public class SessionsFragment extends Fragment {

    private final ArrayList<SessionEntity> sessions = new ArrayList<>();
    private SessionEntityAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sessions, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        RecyclerView rv = view.findViewById(R.id.rvSessions);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new SessionEntityAdapter(
                sessions,
                session -> {
                    Intent i = new Intent(requireContext(), EditorActivity.class);
                    i.putExtra("sessionId", session.id);
                    startActivity(i);
                },
                this::showSessionMenu
        );
        rv.setAdapter(adapter);

        AppDatabase db = AppDatabase.getInstance(requireContext());

        db.gymDao().observeSessions().observe(getViewLifecycleOwner(), list -> {
            sessions.clear();
            if (list != null) sessions.addAll(list);
            adapter.notifyDataSetChanged();
        });

        Button btnNew = view.findViewById(R.id.btnNewSession);
        btnNew.setOnClickListener(v -> {
            DbExecutors.IO.execute(() -> {
                SessionEntity s = new SessionEntity();
                s.title = "New Session";
                s.dateEpochMillis = System.currentTimeMillis();
                long id = db.gymDao().insertSession(s);

                requireActivity().runOnUiThread(() -> {
                    Intent i = new Intent(requireContext(), EditorActivity.class);
                    i.putExtra("sessionId", id);
                    startActivity(i);
                });
            });
        });
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