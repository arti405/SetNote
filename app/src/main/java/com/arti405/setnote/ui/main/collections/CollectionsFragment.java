package com.arti405.setnote.ui.main.collections;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.*;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.arti405.setnote.MainActivity;
import com.arti405.setnote.R;
import com.arti405.setnote.data.AppDatabase;
import com.arti405.setnote.data.CollectionEntity;
import com.arti405.setnote.data.DbExecutors;

import java.util.ArrayList;

public class CollectionsFragment extends Fragment {

    private final ArrayList<CollectionEntity> collections = new ArrayList<>();
    private SimpleCollectionAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_collections, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        ((MainActivity) requireActivity()).setToolbarTitle("Collections");

        RecyclerView rv = view.findViewById(R.id.rvCollections);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new SimpleCollectionAdapter(
                collections,
                c -> ((MainActivity) requireActivity()).openFragment(
                        CollectionDetailFragment.newInstance(c.id, c.name),
                        true
                ),
                this::showCollectionMenu
        );

        rv.setAdapter(adapter);

        AppDatabase.getInstance(requireContext())
                .gymDao()
                .observeCollections()
                .observe(getViewLifecycleOwner(), list -> {
                    collections.clear();
                    if (list != null) collections.addAll(list);
                    adapter.notifyDataSetChanged();
                });
        view.findViewById(R.id.btnCollectionsPlus).setOnClickListener(v -> showCreateCollectionDialog());
    }

    private void showCollectionMenu(View anchor, CollectionEntity c) {

        androidx.appcompat.widget.PopupMenu popup =
                new androidx.appcompat.widget.PopupMenu(requireContext(), anchor);

        popup.getMenu().add("Rename");
        popup.getMenu().add("Delete");

        popup.setOnMenuItemClickListener(item -> {

            String title = item.getTitle().toString();

            if ("Rename".equals(title)) {
                showRenameDialog(c);
                return true;
            }

            if ("Delete".equals(title)) {
                showDeleteDialog(c);
                return true;
            }

            return false;
        });

        popup.show();
    }

    private void showRenameDialog(CollectionEntity c) {
        EditText input = new EditText(requireContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        input.setText(c.name == null ? "" : c.name);
        input.setSelection(input.getText().length());

        new AlertDialog.Builder(requireContext())
                .setTitle("Rename collection")
                .setView(input)
                .setNegativeButton("Cancel", (d, w) -> d.dismiss())
                .setPositiveButton("Save", (d, w) -> {
                    String name = input.getText().toString().trim();
                    if (name.isEmpty()) name = "New Collection";

                    String finalName = name;
                    DbExecutors.IO.execute(() ->
                            AppDatabase.getInstance(requireContext()).gymDao().renameCollection(c.id, finalName)
                    );
                })
                .show();
    }

    private void showDeleteDialog(CollectionEntity c) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete collection?")
                .setMessage("This removes the folder only. Sessions stay safe in the archive.")
                .setNegativeButton("Cancel", (d, w) -> d.dismiss())
                .setPositiveButton("Delete", (d, w) -> DbExecutors.IO.execute(() ->
                        AppDatabase.getInstance(requireContext()).gymDao().deleteCollection(c.id)
                ))
                .show();
    }

    private void showCreateCollectionDialog() {
        android.widget.EditText input = new android.widget.EditText(requireContext());
        input.setHint("Collection name");

        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("New Collection")
                .setView(input)
                .setNegativeButton("Cancel", (d, w) -> d.dismiss())
                .setPositiveButton("Create", (d, w) -> {
                    String name = input.getText().toString().trim();
                    if (name.isEmpty()) name = "New Collection";

                    String finalName = name;
                    DbExecutors.IO.execute(() -> {
                        CollectionEntity c = new CollectionEntity();
                        c.name = finalName;
                        c.createdAt = System.currentTimeMillis();

                        AppDatabase
                                .getInstance(requireContext())
                                .gymDao()
                                .insertCollection(c);
                    });
                })
                .show();
    }
}
