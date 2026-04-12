package com.arti405.setnote.ui.main.archive;

import android.content.Intent;
import android.os.Bundle;
import android.view.*;
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

public class MonthSessionsFragment extends Fragment {

    private static final String ARG_MONTH_FOLDER_ID = "monthFolderId";
    private static final String ARG_MONTH_NAME = "monthName";

    public static MonthSessionsFragment newInstance(long monthFolderId, String monthName) {
        MonthSessionsFragment f = new MonthSessionsFragment();
        Bundle b = new Bundle();
        b.putLong(ARG_MONTH_FOLDER_ID, monthFolderId);
        b.putString(ARG_MONTH_NAME, monthName);
        f.setArguments(b);
        return f;
    }

    private final ArrayList<SessionEntity> sessions = new ArrayList<>();
    private SimpleSessionAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_simple_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        long monthFolderId = requireArguments().getLong(ARG_MONTH_FOLDER_ID);
        String monthName = requireArguments().getString(ARG_MONTH_NAME, "Month");

        ((MainActivity) requireActivity()).setToolbarTitle(monthName);

        RecyclerView rv = view.findViewById(R.id.rv);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new SimpleSessionAdapter(sessions, s -> {
                    Intent i = new Intent(requireContext(), EditorActivity.class);
                    i.putExtra("sessionId", s.id);
                    startActivity(i);
                },
                this::showSessionMenu
        );
        rv.setAdapter(adapter);

        AppDatabase.getInstance(requireContext())
                .gymDao()
                .observeSessionsForMonth(monthFolderId)
                .observe(getViewLifecycleOwner(), list -> {
                    sessions.clear();
                    if (list != null) sessions.addAll(list);
                    adapter.notifyDataSetChanged();
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
