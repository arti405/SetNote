package com.arti405.setnote.ui.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.arti405.setnote.data.AppDatabase;
import com.arti405.setnote.data.CollectionEntity;
import com.arti405.setnote.data.DbExecutors;
import com.arti405.setnote.ui.main.archive.CalendarDayAdapter;
import com.arti405.setnote.ui.main.archive.CalendarDayItem;
import com.arti405.setnote.ui.main.archive.DaySessionsFragment;
import com.arti405.setnote.ui.main.collections.CollectionDetailFragment;
import com.arti405.setnote.ui.main.collections.SimpleCollectionAdapter;

import com.arti405.setnote.R;
import com.arti405.setnote.ui.main.archive.TemplatesFragment;
import com.arti405.setnote.ui.main.archive.YearsFragment;

public class HubFragment extends Fragment {

    private boolean archiveExpanded = true;
    private final java.util.Calendar hubMonth = java.util.Calendar.getInstance();
    private final java.util.ArrayList<CalendarDayItem> hubDayItems = new java.util.ArrayList<>();
    private CalendarDayAdapter hubCalendarAdapter;
    private Integer selectedHubDay = null;

    private android.widget.TextView tvHubMonthLabel;
    private boolean collectionsExpanded = false;
    private final java.util.ArrayList<CollectionEntity> hubCollections = new java.util.ArrayList<>();
    private SimpleCollectionAdapter hubCollectionsAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_hub, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        View archiveHeader = view.findViewById(R.id.archiveHeader);
        View archiveContent = view.findViewById(R.id.archiveContent);
        TextView tvArchiveToggle = view.findViewById(R.id.tvArchiveToggle);

        updateArchiveAccordion(archiveContent, tvArchiveToggle);

        archiveHeader.setOnClickListener(v -> {
            archiveExpanded = !archiveExpanded;
            updateArchiveAccordion(archiveContent, tvArchiveToggle);
        });

        // Archive navigation
        view.findViewById(R.id.rowAllSessions).setOnClickListener(v ->
                ((MainActivity) requireActivity()).openFragment(new SessionsFragment(), true)
        );

        view.findViewById(R.id.rowTemplates).setOnClickListener(v ->
                ((MainActivity) requireActivity()).openFragment(new TemplatesFragment(), true)
        );

        view.findViewById(R.id.rowYearMonth).setOnClickListener(v ->
                ((MainActivity) requireActivity()).openFragment(new YearsFragment(), true)
        );

        // Quick Access
        view.findViewById(R.id.rowStartSession).setOnClickListener(v ->
                ((MainActivity) requireActivity()).createSessionAutoFolderAndOpenEditor()
        );

        view.findViewById(R.id.rowStartTemplate).setOnClickListener(v ->
                ((MainActivity) requireActivity()).showTemplatePicker()
        );

        // Calendar Preview
        tvHubMonthLabel = view.findViewById(R.id.tvHubMonthLabel);

        androidx.recyclerview.widget.RecyclerView rvHubCalendar = view.findViewById(R.id.rvHubCalendar);
        rvHubCalendar.setLayoutManager(new androidx.recyclerview.widget.GridLayoutManager(requireContext(), 7));

        hubCalendarAdapter = new CalendarDayAdapter(
                hubDayItems,
                this::openHubDay
        );
        rvHubCalendar.setAdapter(hubCalendarAdapter);

        view.findViewById(R.id.btnHubPrevMonth).setOnClickListener(v -> {
            hubMonth.add(java.util.Calendar.MONTH, -1);
            selectedHubDay = null;
            refreshHubCalendar();
        });

        view.findViewById(R.id.btnHubNextMonth).setOnClickListener(v -> {
            hubMonth.add(java.util.Calendar.MONTH, 1);
            selectedHubDay = null;
            refreshHubCalendar();
        });

        refreshHubCalendar();

        View collectionsHeader = view.findViewById(R.id.collectionsHeader);
        View collectionsContent = view.findViewById(R.id.collectionsContent);
        View btnHubAddCollection = view.findViewById(R.id.btnHubAddCollection);
        btnHubAddCollection.setOnClickListener(v -> showHubCreateCollectionDialog());
        android.widget.TextView tvCollectionsSectionToggle = view.findViewById(R.id.tvCollectionsSectionToggle);
        androidx.recyclerview.widget.RecyclerView rvHubCollections = view.findViewById(R.id.rvHubCollections);

        rvHubCollections.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(requireContext()));
        rvHubCollections.setNestedScrollingEnabled(false);

        hubCollectionsAdapter = new SimpleCollectionAdapter(
                hubCollections,
                c -> ((MainActivity) requireActivity()).openFragment(
                        CollectionDetailFragment.newInstance(c.id, c.name),
                        true
                ),
                this::showHubCollectionMenu
        );

        rvHubCollections.setAdapter(hubCollectionsAdapter);

        updateCollectionsSection(collectionsContent, tvCollectionsSectionToggle);

        collectionsHeader.setOnClickListener(v -> {
            collectionsExpanded = !collectionsExpanded;
            updateCollectionsSection(collectionsContent, tvCollectionsSectionToggle);
        });

        AppDatabase.getInstance(requireContext())
                .gymDao()
                .observeCollections()
                .observe(getViewLifecycleOwner(), list -> {
                    hubCollections.clear();
                    if (list != null) hubCollections.addAll(list);
                    hubCollectionsAdapter.notifyDataSetChanged();
                });

    }
    private void showHubCreateCollectionDialog() {
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
    private void updateCollectionsSection(View collectionsContent, android.widget.TextView tvCollectionsSectionToggle) {
        collectionsContent.setVisibility(collectionsExpanded ? View.VISIBLE : View.GONE);
        tvCollectionsSectionToggle.setText(collectionsExpanded ? "▼" : "▶");
    }
    private void showHubCollectionMenu(View anchor, CollectionEntity c) {
        androidx.appcompat.widget.PopupMenu popup =
                new androidx.appcompat.widget.PopupMenu(requireContext(), anchor);

        popup.getMenu().add("Rename");
        popup.getMenu().add("Delete");

        popup.setOnMenuItemClickListener(item -> {
            String title = item.getTitle().toString();

            if ("Rename".equals(title)) {
                showHubCollectionRenameDialog(c);
                return true;
            }

            if ("Delete".equals(title)) {
                showHubCollectionDeleteDialog(c);
                return true;
            }

            return false;
        });

        popup.show();
    }
    private void showHubCollectionRenameDialog(CollectionEntity c) {
        android.widget.EditText input = new android.widget.EditText(requireContext());
        input.setText(c.name == null ? "" : c.name);
        input.setSelection(input.getText().length());

        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Rename Collection")
                .setView(input)
                .setNegativeButton("Cancel", (d, w) -> d.dismiss())
                .setPositiveButton("Save", (d, w) -> {
                    String newName = input.getText().toString().trim();
                    if (newName.isEmpty()) newName = "New Collection";

                    String finalName = newName;
                    DbExecutors.IO.execute(() ->
                            AppDatabase
                                    .getInstance(requireContext())
                                    .gymDao()
                                    .renameCollection(c.id, finalName)
                    );
                })
                .show();
    }
    private void showHubCollectionDeleteDialog(CollectionEntity c) {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Delete Collection?")
                .setMessage("This removes the collection only. Sessions stay safe in the archive.")
                .setNegativeButton("Cancel", (d, w) -> d.dismiss())
                .setPositiveButton("Delete", (d, w) -> {
                    DbExecutors.IO.execute(() ->
                            AppDatabase
                                    .getInstance(requireContext())
                                    .gymDao()
                                    .deleteCollection(c.id)
                    );
                })
                .show();
    }

    private void refreshHubCalendar() {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMMM yyyy", java.util.Locale.getDefault());
        tvHubMonthLabel.setText(sdf.format(hubMonth.getTime()));
        loadHubMarkedDaysForCurrentMonth();
    }
    private void loadHubMarkedDaysForCurrentMonth() {
        long start = getStartOfMonthMillis(hubMonth);
        long end = getEndOfMonthMillis(hubMonth);

        DbExecutors.IO.execute(() -> {
            java.util.List<Long> timestamps = AppDatabase
                    .getInstance(requireContext())
                    .gymDao()
                    .getSessionDatesInRange(start, end);

            java.util.Set<Integer> markedDays = new java.util.LinkedHashSet<>();

            if (timestamps != null) {
                for (Long ts : timestamps) {
                    java.util.Calendar c = java.util.Calendar.getInstance();
                    c.setTimeInMillis(ts);
                    markedDays.add(c.get(java.util.Calendar.DAY_OF_MONTH));
                }
            }

            java.util.ArrayList<Integer> result = new java.util.ArrayList<>(markedDays);

            requireActivity().runOnUiThread(() -> buildHubCalendarGrid(result));
        });
    }
    private void buildHubCalendarGrid(java.util.List<Integer> markedDays) {
        hubDayItems.clear();

        java.util.Set<Integer> markedSet = new java.util.HashSet<>();
        if (markedDays != null) markedSet.addAll(markedDays);

        java.util.Calendar cal = (java.util.Calendar) hubMonth.clone();
        cal.set(java.util.Calendar.DAY_OF_MONTH, 1);

        int firstDayOfWeek = cal.get(java.util.Calendar.DAY_OF_WEEK);
        int offset = firstDayOfWeek - java.util.Calendar.MONDAY;
        if (offset < 0) offset += 7;

        for (int i = 0; i < offset; i++) {
            hubDayItems.add(new CalendarDayItem("", null, false, false, false));
        }

        int maxDay = cal.getActualMaximum(java.util.Calendar.DAY_OF_MONTH);

        java.util.Calendar todayCal = java.util.Calendar.getInstance();
        int todayYear = todayCal.get(java.util.Calendar.YEAR);
        int todayMonth = todayCal.get(java.util.Calendar.MONTH);
        int todayDay = todayCal.get(java.util.Calendar.DAY_OF_MONTH);

        int shownYear = hubMonth.get(java.util.Calendar.YEAR);
        int shownMonth = hubMonth.get(java.util.Calendar.MONTH);

        for (int day = 1; day <= maxDay; day++) {
            boolean marked = markedSet.contains(day);
            boolean today = (shownYear == todayYear && shownMonth == todayMonth && day == todayDay);
            boolean selected = (selectedHubDay != null && selectedHubDay == day);

            hubDayItems.add(new CalendarDayItem(
                    String.valueOf(day),
                    day,
                    marked,
                    today,
                    selected
            ));
        }

        hubCalendarAdapter.notifyDataSetChanged();
    }

    private void openHubDay(CalendarDayItem item) {
        if (item == null || item.dayNumber == null) return;

        selectedHubDay = item.dayNumber;
        refreshHubCalendar();

        java.util.Calendar c = (java.util.Calendar) hubMonth.clone();
        c.set(java.util.Calendar.DAY_OF_MONTH, item.dayNumber);
        c.set(java.util.Calendar.HOUR_OF_DAY, 0);
        c.set(java.util.Calendar.MINUTE, 0);
        c.set(java.util.Calendar.SECOND, 0);
        c.set(java.util.Calendar.MILLISECOND, 0);
        long start = c.getTimeInMillis();

        c.set(java.util.Calendar.HOUR_OF_DAY, 23);
        c.set(java.util.Calendar.MINUTE, 59);
        c.set(java.util.Calendar.SECOND, 59);
        c.set(java.util.Calendar.MILLISECOND, 999);
        long end = c.getTimeInMillis();

        String label = new java.text.SimpleDateFormat("dd MMMM yyyy", java.util.Locale.getDefault())
                .format(new java.util.Date(start));

        ((MainActivity) requireActivity()).openFragment(
                DaySessionsFragment.newInstance(start, end, label),
                true
        );
    }
    private long getStartOfMonthMillis(java.util.Calendar source) {
        java.util.Calendar c = (java.util.Calendar) source.clone();
        c.set(java.util.Calendar.DAY_OF_MONTH, 1);
        c.set(java.util.Calendar.HOUR_OF_DAY, 0);
        c.set(java.util.Calendar.MINUTE, 0);
        c.set(java.util.Calendar.SECOND, 0);
        c.set(java.util.Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }

    private long getEndOfMonthMillis(java.util.Calendar source) {
        java.util.Calendar c = (java.util.Calendar) source.clone();
        c.set(java.util.Calendar.DAY_OF_MONTH, c.getActualMaximum(java.util.Calendar.DAY_OF_MONTH));
        c.set(java.util.Calendar.HOUR_OF_DAY, 23);
        c.set(java.util.Calendar.MINUTE, 59);
        c.set(java.util.Calendar.SECOND, 59);
        c.set(java.util.Calendar.MILLISECOND, 999);
        return c.getTimeInMillis();
    }
    private void updateArchiveAccordion(View archiveContent, TextView tvArchiveToggle) {
        archiveContent.setVisibility(archiveExpanded ? View.VISIBLE : View.GONE);
        tvArchiveToggle.setText(archiveExpanded ? "▼" : "▶");
    }
}