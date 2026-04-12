package com.arti405.setnote.ui.main.archive;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.arti405.setnote.MainActivity;
import com.arti405.setnote.R;
import com.arti405.setnote.data.AppDatabase;
import com.arti405.setnote.data.DbExecutors;
import com.arti405.setnote.data.SessionEntity;
import com.arti405.setnote.ui.sessions.EditorActivity;

import java.util.ArrayList;

public class DaySessionsFragment extends Fragment {

    private static final String ARG_START = "start";
    private static final String ARG_END = "end";
    private static final String ARG_LABEL = "label";

    public static DaySessionsFragment newInstance(long startMillis, long endMillis, String label) {
        DaySessionsFragment f = new DaySessionsFragment();
        Bundle b = new Bundle();
        b.putLong(ARG_START, startMillis);
        b.putLong(ARG_END, endMillis);
        b.putString(ARG_LABEL, label);
        f.setArguments(b);
        return f;
    }

    private final ArrayList<SessionEntity> sessions = new ArrayList<>();
    private SimpleSessionAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_simple_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        long start = requireArguments().getLong(ARG_START);
        long end = requireArguments().getLong(ARG_END);
        String label = requireArguments().getString(ARG_LABEL, "Day");

        ((MainActivity) requireActivity()).setToolbarTitle(label);

        RecyclerView rv = view.findViewById(R.id.rv);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        android.widget.TextView tvEmpty = view.findViewById(R.id.tvEmpty);

        adapter = new SimpleSessionAdapter(
                sessions,
                session -> {
                    Intent i = new Intent(requireContext(), EditorActivity.class);
                    i.putExtra("sessionId", session.id);
                    startActivity(i);
                },
                this::showSessionMenu
        );
        rv.setAdapter(adapter);

        AppDatabase.getInstance(requireContext())
                .gymDao()
                .observeSessionsForDay(start, end)
                .observe(getViewLifecycleOwner(), list -> {
                    sessions.clear();
                    if (list != null) sessions.addAll(list);
                    adapter.notifyDataSetChanged();

                    if (sessions.isEmpty()) {
                        tvEmpty.setVisibility(View.VISIBLE);
                        tvEmpty.setText("No sessions on this day");
                        rv.setVisibility(View.GONE);
                    } else {
                        tvEmpty.setVisibility(View.GONE);
                        rv.setVisibility(View.VISIBLE);
                    }
                });
    }

    private void showSessionMenu(View anchor, SessionEntity session) {
        androidx.appcompat.widget.PopupMenu popup =
                new androidx.appcompat.widget.PopupMenu(requireContext(), anchor);

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

    private void showRenameDialog(SessionEntity session) {
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
                    DbExecutors.IO.execute(() ->
                            AppDatabase
                                    .getInstance(requireContext())
                                    .gymDao()
                                    .renameSession(session.id, finalTitle)
                    );
                })
                .show();
    }

    private void showDeleteDialog(SessionEntity session) {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Delete Session?")
                .setMessage("This deletes the session permanently.")
                .setNegativeButton("Cancel", (d, w) -> d.dismiss())
                .setPositiveButton("Delete", (d, w) -> {
                    DbExecutors.IO.execute(() ->
                            AppDatabase
                                    .getInstance(requireContext())
                                    .gymDao()
                                    .deleteSession(session.id)
                    );
                })
                .show();
    }
}
