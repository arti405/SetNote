package com.example.setnote.ui.editor;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.setnote.R;

import java.util.List;

public class EntryAdapter extends RecyclerView.Adapter<EntryAdapter.VH> {

    private final List<EntryRow> rows;

    public EntryAdapter(List<EntryRow> rows) {
        this.rows = rows;
        setHasStableIds(false);
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_entry_row, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        EntryRow row = rows.get(position);

        // Wichtig: erst Listener lösen, dann Text setzen, dann Listener neu setzen
        h.detachWatchers();

        h.etExercise.setText(row.exercise);
        h.etWeight.setText(row.weight);
        h.etReps.setText(row.reps);
        h.etSets.setText(row.sets);
        h.etNote.setText(row.note);

        h.attachWatchers(row);

        h.btnDelete.setOnClickListener(v -> {
            int p = h.getBindingAdapterPosition();
            if (p == RecyclerView.NO_POSITION) return;
            rows.remove(p);
            notifyItemRemoved(p);
        });

        h.btnDuplicate.setOnClickListener(v -> {
            int p = h.getBindingAdapterPosition();
            if (p == RecyclerView.NO_POSITION) return;
            rows.add(p + 1, new EntryRow(rows.get(p)));
            notifyItemInserted(p + 1);
        });
    }

    @Override
    public int getItemCount() {
        return rows.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        EditText etExercise, etWeight, etReps, etSets, etNote;
        ImageButton btnDuplicate, btnDelete;

        TextWatcher wExercise, wWeight, wReps, wSets, wNote;

        VH(@NonNull View itemView) {
            super(itemView);
            etExercise = itemView.findViewById(R.id.etExercise);
            etWeight = itemView.findViewById(R.id.etWeight);
            etReps = itemView.findViewById(R.id.etReps);
            etSets = itemView.findViewById(R.id.etSets);
            etNote = itemView.findViewById(R.id.etNote);
            btnDuplicate = itemView.findViewById(R.id.btnDuplicate);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }

        void detachWatchers() {
            if (wExercise != null) etExercise.removeTextChangedListener(wExercise);
            if (wWeight != null) etWeight.removeTextChangedListener(wWeight);
            if (wReps != null) etReps.removeTextChangedListener(wReps);
            if (wSets != null) etSets.removeTextChangedListener(wSets);
            if (wNote != null) etNote.removeTextChangedListener(wNote);
        }

        void attachWatchers(EntryRow row) {
            wExercise = simpleWatcher(s -> row.exercise = s);
            wWeight   = simpleWatcher(s -> row.weight   = s);
            wReps     = simpleWatcher(s -> row.reps     = s);
            wSets     = simpleWatcher(s -> row.sets     = s);
            wNote     = simpleWatcher(s -> row.note     = s);

            etExercise.addTextChangedListener(wExercise);
            etWeight.addTextChangedListener(wWeight);
            etReps.addTextChangedListener(wReps);
            etSets.addTextChangedListener(wSets);
            etNote.addTextChangedListener(wNote);
        }

        interface Setter { void set(String s); }

        static TextWatcher simpleWatcher(Setter setter) {
            return new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    setter.set(s.toString());
                }
                @Override public void afterTextChanged(Editable s) {}
            };
        }
    }
}
