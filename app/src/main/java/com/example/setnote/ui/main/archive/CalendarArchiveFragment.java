package com.example.setnote.ui.main.archive;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.setnote.MainActivity;
import com.example.setnote.R;
import com.example.setnote.data.AppDatabase;
import com.example.setnote.data.DbExecutors;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class CalendarArchiveFragment extends Fragment {

    private final Calendar currentMonth = Calendar.getInstance();

    private TextView tvMonthLabel;
    private TextView tvMarkedDays;
    private androidx.recyclerview.widget.RecyclerView rvCalendar;
    private final java.util.ArrayList<CalendarDayItem> dayItems = new java.util.ArrayList<>();
    private CalendarDayAdapter adapter;
    private Integer selectedDay = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_calendar_archive, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        ((MainActivity) requireActivity()).setToolbarTitle("Calendar Archive");

        tvMonthLabel = view.findViewById(R.id.tvMonthLabel);
        rvCalendar = view.findViewById(R.id.rvCalendar);
        rvCalendar.setLayoutManager(new androidx.recyclerview.widget.GridLayoutManager(requireContext(), 7));

        adapter = new CalendarDayAdapter(dayItems, this::openDay);
        rvCalendar.setAdapter(adapter);

        Button btnPrev = view.findViewById(R.id.btnPrevMonth);
        Button btnNext = view.findViewById(R.id.btnNextMonth);

        btnPrev.setOnClickListener(v -> {
            currentMonth.add(Calendar.MONTH, -1);
            refreshMonth();
        });

        btnNext.setOnClickListener(v -> {
            currentMonth.add(Calendar.MONTH, 1);
            refreshMonth();
        });

        btnPrev.setOnClickListener(v -> {
            currentMonth.add(Calendar.MONTH, -1);
            refreshMonth();
        });

        btnNext.setOnClickListener(v -> {
            currentMonth.add(Calendar.MONTH, 1);
            refreshMonth();
        });

        btnPrev.setOnClickListener(v -> {
            currentMonth.add(Calendar.MONTH, -1);
            selectedDay = null;
            refreshMonth();
        });

        btnNext.setOnClickListener(v -> {
            currentMonth.add(Calendar.MONTH, 1);
            selectedDay = null;
            refreshMonth();
        });

        refreshMonth();
    }

    private void refreshMonth() {
        updateMonthLabel();
        loadMarkedDaysForCurrentMonth();
    }

    private void updateMonthLabel() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        tvMonthLabel.setText(sdf.format(currentMonth.getTime()));
    }

    private void loadMarkedDaysForCurrentMonth() {
        long start = getStartOfMonthMillis(currentMonth);
        long end = getEndOfMonthMillis(currentMonth);

        DbExecutors.IO.execute(() -> {
            List<Long> timestamps = AppDatabase.getInstance(requireContext())
                    .gymDao()
                    .getSessionDatesInRange(start, end);

            Set<Integer> markedDays = new LinkedHashSet<>();

            if (timestamps != null) {
                for (Long ts : timestamps) {
                    Calendar c = Calendar.getInstance();
                    c.setTimeInMillis(ts);
                    markedDays.add(c.get(Calendar.DAY_OF_MONTH));
                }
            }

            ArrayList<Integer> result = new ArrayList<>(markedDays);

            requireActivity().runOnUiThread(() -> buildCalendarGrid(result));
        });
    }
    private void buildCalendarGrid(List<Integer> markedDays) {
        dayItems.clear();

        java.util.Set<Integer> markedSet = new java.util.HashSet<>();
        if (markedDays != null) markedSet.addAll(markedDays);

        Calendar cal = (Calendar) currentMonth.clone();
        cal.set(Calendar.DAY_OF_MONTH, 1);

        int firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        int offset = firstDayOfWeek - Calendar.MONDAY;
        if (offset < 0) offset += 7;

        for (int i = 0; i < offset; i++) {
            dayItems.add(new CalendarDayItem("", null, false, false, false));
        }

        int maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

        Calendar todayCal = Calendar.getInstance();
        int todayYear = todayCal.get(Calendar.YEAR);
        int todayMonth = todayCal.get(Calendar.MONTH);
        int todayDay = todayCal.get(Calendar.DAY_OF_MONTH);

        int shownYear = currentMonth.get(Calendar.YEAR);
        int shownMonth = currentMonth.get(Calendar.MONTH);

        for (int day = 1; day <= maxDay; day++) {
            boolean marked = markedSet.contains(day);
            boolean today = (shownYear == todayYear && shownMonth == todayMonth && day == todayDay);
            boolean selected = (selectedDay != null && selectedDay == day);

            dayItems.add(new CalendarDayItem(
                    String.valueOf(day),
                    day,
                    marked,
                    today,
                    selected
            ));
        }

        adapter.notifyDataSetChanged();
    }
    private void showMarkedDays(List<Integer> days) {
        if (days == null || days.isEmpty()) {
            tvMarkedDays.setText("No sessions in this month");
            return;
        }

        StringBuilder sb = new StringBuilder("Marked days: ");
        for (int i = 0; i < days.size(); i++) {
            sb.append(days.get(i));
            if (i < days.size() - 1) sb.append(", ");
        }

        tvMarkedDays.setText(sb.toString());
    }

    private long getStartOfMonthMillis(Calendar source) {
        Calendar c = (Calendar) source.clone();
        c.set(Calendar.DAY_OF_MONTH, 1);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }

    private long getEndOfMonthMillis(Calendar source) {
        Calendar c = (Calendar) source.clone();
        c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH));
        c.set(Calendar.HOUR_OF_DAY, 23);
        c.set(Calendar.MINUTE, 59);
        c.set(Calendar.SECOND, 59);
        c.set(Calendar.MILLISECOND, 999);
        return c.getTimeInMillis();
    }

    private void openDay(CalendarDayItem item) {

        if (item == null || item.dayNumber == null) return;

        selectedDay = item.dayNumber;
        refreshMonth();

        Calendar c = (Calendar) currentMonth.clone();
        c.set(Calendar.DAY_OF_MONTH, item.dayNumber);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        long start = c.getTimeInMillis();

        c.set(Calendar.HOUR_OF_DAY, 23);
        c.set(Calendar.MINUTE, 59);
        c.set(Calendar.SECOND, 59);
        c.set(Calendar.MILLISECOND, 999);
        long end = c.getTimeInMillis();

        String label = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(new Date(start));

        ((MainActivity) requireActivity()).openFragment(
                DaySessionsFragment.newInstance(start, end, label),
                true
        );
    }
}
