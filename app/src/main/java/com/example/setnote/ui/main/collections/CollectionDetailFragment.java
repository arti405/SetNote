package com.example.setnote.ui.main.collections;

import android.content.Intent;
import android.os.Bundle;
import android.view.*;
import android.widget.Button;

import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.setnote.MainActivity;
import com.example.setnote.R;
import com.example.setnote.data.*;

import com.example.setnote.ui.main.archive.SimpleSessionAdapter;
import com.example.setnote.ui.sessions.EditorActivity;

import java.util.ArrayList;

public class CollectionDetailFragment extends Fragment {

    private static final String ARG_COLLECTION_ID = "collectionId";
    private static final String ARG_COLLECTION_NAME = "collectionName";

    public static CollectionDetailFragment newInstance(long id, String name) {
        CollectionDetailFragment f = new CollectionDetailFragment();
        Bundle b = new Bundle();
        b.putLong(ARG_COLLECTION_ID, id);
        b.putString(ARG_COLLECTION_NAME, name);
        f.setArguments(b);
        return f;
    }

    private long collectionId;
    private final ArrayList<SessionEntity> sessions = new ArrayList<>();
    private SimpleSessionAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_collection_detail, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        collectionId = requireArguments().getLong(ARG_COLLECTION_ID);
        String name = requireArguments().getString(ARG_COLLECTION_NAME, "Collection");

        ((MainActivity) requireActivity()).setToolbarTitle(name);

        RecyclerView rv = view.findViewById(R.id.rvSessions);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new SimpleSessionAdapter(
                sessions,
                this::openSession,
                this::showSessionMenu
        );

        rv.setAdapter(adapter);

        AppDatabase.getInstance(requireContext())
                .gymDao()
                .observeSessionsInCollection(collectionId)
                .observe(getViewLifecycleOwner(), list -> {

                    sessions.clear();
                    if (list != null) sessions.addAll(list);

                    adapter.notifyDataSetChanged();
                });

        Button btnAdd = view.findViewById(R.id.btnAddSession);
        btnAdd.setOnClickListener(v -> showAddSessionDialog());


    }
    private void showSessionMenu(View anchor, SessionEntity session) {
        androidx.appcompat.widget.PopupMenu popup =
                new androidx.appcompat.widget.PopupMenu(requireContext(), anchor);

        popup.getMenu().add("Remove from collection");
        popup.getMenu().add("Delete permanently");

        popup.setOnMenuItemClickListener(item -> {
            String title = item.getTitle().toString();

            if ("Remove from collection".equals(title)) {
                removeFromCollection(session);
                return true;
            }

            if ("Delete permanently".equals(title)) {
                showDeleteDialog(session);
                return true;
            }

            return false;
        });

        popup.show();
    }



    private void openSession(SessionEntity s) {

        Intent i = new Intent(requireContext(), EditorActivity.class);
        i.putExtra("sessionId", s.id);

        startActivity(i);
    }

    private void removeFromCollection(SessionEntity s) {

        DbExecutors.IO.execute(() -> {

            AppDatabase.getInstance(requireContext())
                    .gymDao()
                    .removeSessionFromCollection(collectionId, s.id);

        });
    }

    private void showDeleteDialog(SessionEntity session) {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Delete Session?")
                .setMessage("This deletes the session permanently.")
                .setNegativeButton("Cancel", (d, w) -> d.dismiss())
                .setPositiveButton("Delete", (d, w) -> {
                    DbExecutors.IO.execute(() ->
                            AppDatabase.getInstance(requireContext())
                                    .gymDao()
                                    .deleteSession(session.id)
                    );
                })
                .show();
    }

    private void showAddSessionDialog() {

        DbExecutors.IO.execute(() -> {

            GymDao dao = AppDatabase.getInstance(requireContext()).gymDao();

            java.util.List<SessionEntity> all = dao.getAllSessions();

            requireActivity().runOnUiThread(() -> {

                String[] names = new String[all.size()];

                for (int i = 0; i < all.size(); i++) {

                    String t = all.get(i).title;

                    names[i] = (t == null || t.trim().isEmpty())
                            ? "Session"
                            : t;
                }

                new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                        .setTitle("Add Session")
                        .setItems(names, (d, which) -> {

                            SessionEntity s = all.get(which);

                            DbExecutors.IO.execute(() -> {

                                int pos = dao.nextCollectionPosition(collectionId);

                                CollectionSessionEntity link = new CollectionSessionEntity();

                                link.collectionId = collectionId;
                                link.sessionId = s.id;
                                link.position = pos;

                                dao.insertCollectionSession(link);

                            });

                        })
                        .show();
            });
        });
    }
}
