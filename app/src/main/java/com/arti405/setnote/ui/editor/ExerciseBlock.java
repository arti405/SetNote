package com.arti405.setnote.ui.editor;

import java.util.ArrayList;

public class ExerciseBlock {
    public String exercise = "";
    public final ArrayList<SetRow> sets = new ArrayList<>();

    public ExerciseBlock() {
        sets.add(new SetRow()); // Start: 1 Set
    }

    public ExerciseBlock(ExerciseBlock other) {
        this.exercise = other.exercise;
        for (SetRow s : other.sets) sets.add(new SetRow(s));
    }
}
