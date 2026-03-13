package com.example.tabnotes.ui.editor;

public class EntryRow {
    public String exercise = "";
    public String weight = "";
    public String reps = "";
    public String sets = "";
    public String note = "";

    public EntryRow() {}

    public EntryRow(EntryRow other) {
        this.exercise = other.exercise;
        this.weight = other.weight;
        this.reps = other.reps;
        this.sets = other.sets;
        this.note = other.note;
    }
}

