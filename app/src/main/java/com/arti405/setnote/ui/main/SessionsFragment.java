package com.arti405.setnote.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.*;
import android.widget.LinearLayout;

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
import com.arti405.setnote.ui.sessions.SessionEntityAdapter;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class SessionsFragment extends Fragment {

    private final ArrayList<SessionEntity> sessions = new ArrayList<>();
    private SessionEntityAdapter adapter;
    private LinearLayout emptyState;

    private boolean selectionMode = false;
    private final Set<Long> selectedIds = new HashSet<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sessions, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        emptyState = view.findViewById(R.id.emptyState);
        RecyclerView rv = view.findViewById(R.id.rvSessions);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new SessionEntityAdapter(
                sessions,
                session -> {
                    Intent i = new Intent(requireContext(), EditorActivity.class);
                    i.putExtra("sessionId", session.id);
                    startActivity(i);
                },
                this::showSessionMenu,
                ids -> updateToolbar()
        );
        rv.setAdapter(adapter);

        AppDatabase db = AppDatabase.getInstance(requireContext());

        db.gymDao().observeSessions().observe(getViewLifecycleOwner(), list -> {
            sessions.clear();
            if (list != null) sessions.addAll(list);
            adapter.notifyDataSetChanged();
            
            if (emptyState != null) {
                emptyState.setVisibility(sessions.isEmpty() ? View.VISIBLE : View.GONE);
            }
        });

        ExtendedFloatingActionButton fabNew = view.findViewById(R.id.fabNewSession);
        fabNew.setOnClickListener(v -> {
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

        updateToolbar();
    }

    private void showSessionMenu(View anchor, SessionEntity session) {
        androidx.appcompat.widget.PopupMenu popup =
                new androidx.appcompat.widget.PopupMenu(requireContext(), anchor);

        if (!selectionMode) {
            popup.getMenu().add("Rename");
            popup.getMenu().add("Delete");
        }

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
                            AppDatabase.getInstance(requireContext())
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
                .setPositiveButton("Delete", (d, w) ->
                        DbExecutors.IO.execute(() ->
                                AppDatabase.getInstance(requireContext())
                                        .gymDao()
                                        .deleteSession(session.id)
                        )
                )
                .show();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        menu.clear();

        if (!selectionMode) {
            menu.add(0, 101, 0, "Select");
        } else {
            menu.add(0, 102, 0, "Delete");
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == 101) {
            startSelectionMode();
            return true;
        }

        if (item.getItemId() == 102) {
            deleteSelected();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void startSelectionMode() {
        selectionMode = true;
        selectedIds.clear();

        adapter.setSelectionMode(true);
        adapter.setSelectedIds(selectedIds);

        updateToolbar();
        requireActivity().invalidateOptionsMenu();
    }

    private void exitSelectionMode() {
        selectionMode = false;
        selectedIds.clear();

        adapter.setSelectionMode(false);
        adapter.setSelectedIds(selectedIds);

        updateToolbar();
        requireActivity().invalidateOptionsMenu();
    }

    private void updateToolbar() {
        if (selectionMode) {
            ((MainActivity) requireActivity()).setToolbarTitle(selectedIds.size() + " selected");
        } else {
            ((MainActivity) requireActivity()).setToolbarTitle("All Sessions");
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        requireActivity().getOnBackPressedDispatcher().addCallback(
                getViewLifecycleOwner(),
                new androidx.activity.OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        if (selectionMode) {
                            exitSelectionMode();
                        } else {
                            setEnabled(false);
                            requireActivity().onBackPressed();
                        }
                    }
                }
        );
    }

    private void deleteSelected() {
        if (selectedIds.isEmpty()) return;

        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Delete Sessions?")
                .setMessage("This deletes selected sessions.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete", (d, w) -> {
                    java.util.List<Long> ids = new java.util.ArrayList<>(selectedIds);

                    DbExecutors.IO.execute(() ->
                            AppDatabase.getInstance(requireContext())
                                    .gymDao()
                                    .deleteSessionsByIds(ids)
                    );

                    exitSelectionMode();
                })
                .show();
    }
}
