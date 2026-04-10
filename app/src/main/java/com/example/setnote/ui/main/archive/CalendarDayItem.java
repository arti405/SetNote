package com.example.setnote.ui.main.archive;

public class CalendarDayItem {
    public final String label;
    public final Integer dayNumber; // null = leer
    public final boolean marked;
    public final boolean today;
    public final boolean selected;

    public CalendarDayItem(String label, Integer dayNumber, boolean marked, boolean today, boolean selected) {
        this.label = label;
        this.dayNumber = dayNumber;
        this.marked = marked;
        this.today = today;
        this.selected = selected;
    }
}