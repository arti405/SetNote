package com.arti405.setnote.ui.main.archive;

import android.os.Bundle;
import android.view.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.arti405.setnote.ui.main.MainActivity;
import com.arti405.setnote.R;
import com.arti405.setnote.data.AppDatabase;
import com.arti405.setnote.data.FolderEntity;

import java.util.ArrayList;

public class YearsFragment extends Fragment {

    private final ArrayList<FolderEntity> years = new ArrayList<>();
    private SimpleFolderAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_simple_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        ((MainActivity) requireActivity()).setToolbarTitle("Archive");

        RecyclerView rv = view.findViewById(R.id.rv);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new SimpleFolderAdapter(years, folder -> {
            // Templates Fake-Eintrag
            if ("TEMPLATES".equals(folder.type)) {
                ((MainActivity) requireActivity()).openFragment(new TemplatesFragment(), true);
                return;
            }

            MonthsFragment f = MonthsFragment.newInstance(folder.id, folder.name);
            ((MainActivity) requireActivity()).openFragment(f, true);
        });
        rv.setAdapter(adapter);

        AppDatabase.getInstance(requireContext())
                .gymDao()
                .observeYearFolders()
                .observe(getViewLifecycleOwner(), list -> {
                    years.clear();

                    // Fake Folder "Templates" ganz oben


                    if (list != null) years.addAll(list);
                    adapter.notifyDataSetChanged();
                });
    }
}
