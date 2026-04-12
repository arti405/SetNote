package com.arti405.setnote.ui.main.archive;

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
import com.arti405.setnote.ui.main.SessionsFragment;

import java.util.ArrayList;

public class ArchiveRootFragment extends Fragment {

    private final ArrayList<ArchiveRootItem> items = new ArrayList<>();
    private ArchiveRootAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_archive_root, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        ((MainActivity) requireActivity()).setToolbarTitle("Archive");

        RecyclerView rv = view.findViewById(R.id.rvArchiveRoot);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));

        seedItems();

        adapter = new ArchiveRootAdapter(items, this::handleClick);
        rv.setAdapter(adapter);
    }

    private void seedItems() {
        items.clear();
        items.add(new ArchiveRootItem(
                "Templates",
                "Saved workout templates",
                ArchiveRootItem.TYPE_TEMPLATES
        ));
        items.add(new ArchiveRootItem(
                "All Sessions",
                "Every saved session in one place",
                ArchiveRootItem.TYPE_ALL_SESSIONS
        ));
        items.add(new ArchiveRootItem(
                "Year / Month Archive",
                "Browse sessions by year and month",
                ArchiveRootItem.TYPE_YEARS
        ));

        items.add(new ArchiveRootItem(
                "Calendar View",
                "Browse sessions by marked days",
                ArchiveRootItem.TYPE_CALENDAR
        ));
    }

    private void handleClick(ArchiveRootItem item) {
        MainActivity main = (MainActivity) requireActivity();

        switch (item.type) {
            case ArchiveRootItem.TYPE_TEMPLATES:
                main.openFragment(new TemplatesFragment(), true);
                break;

            case ArchiveRootItem.TYPE_ALL_SESSIONS:
                main.openFragment(new SessionsFragment(), true);
                break;

            case ArchiveRootItem.TYPE_YEARS:
                main.openFragment(new YearsFragment(), true);
                break;

            case ArchiveRootItem.TYPE_CALENDAR:
                main.openFragment(new CalendarArchiveFragment(), true);
                break;
        }
    }
}
