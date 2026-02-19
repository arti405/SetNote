package com.example.tabnotes.ui.sessions.editor;

public class SetRow {
    public String weight = "";
    public String reps = "";
    public String note = "";

    public SetRow() {}
    public SetRow(SetRow other) {
        this.weight = other.weight;
        this.reps = other.reps;
        this.note = other.note;
    }
}
