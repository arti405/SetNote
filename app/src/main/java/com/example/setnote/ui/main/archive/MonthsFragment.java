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
import com.example.setnote.data.FolderEntity;

import java.util.ArrayList;

public class MonthsFragment extends Fragment {

    private static final String ARG_YEAR_FOLDER_ID = "yearFolderId";
    private static final String ARG_YEAR_NAME = "yearName";

    public static MonthsFragment newInstance(long yearFolderId, String yearName) {
        MonthsFragment f = new MonthsFragment();
        Bundle b = new Bundle();
        b.putLong(ARG_YEAR_FOLDER_ID, yearFolderId);
        b.putString(ARG_YEAR_NAME, yearName);
        f.setArguments(b);
        return f;
    }

    private final ArrayList<FolderEntity> months = new ArrayList<>();
    private SimpleFolderAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_simple_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        long yearFolderId = requireArguments().getLong(ARG_YEAR_FOLDER_ID);
        String yearName = requireArguments().getString(ARG_YEAR_NAME, "Year");

        ((MainActivity) requireActivity()).setToolbarTitle(yearName);

        RecyclerView rv = view.findViewById(R.id.rv);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new SimpleFolderAdapter(months, folder -> {
            MonthSessionsFragment f = MonthSessionsFragment.newInstance(folder.id, folder.name);
            ((MainActivity) requireActivity()).openFragment(f, true);
        });
        rv.setAdapter(adapter);

        AppDatabase.getInstance(requireContext())
                .gymDao()
                .observeMonthFolders(yearFolderId)
                .observe(getViewLifecycleOwner(), list -> {
                    months.clear();
                    if (list != null) months.addAll(list);
                    adapter.notifyDataSetChanged();
                });
    }
}
