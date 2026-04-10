package com.example.setnote.ui.editor;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.setnote.R;

import java.util.List;

public class ExerciseAdapter extends RecyclerView.Adapter<ExerciseAdapter.VH> {

    private final List<ExerciseBlock> blocks;
    private final RecyclerView.RecycledViewPool pool = new RecyclerView.RecycledViewPool();

    public ExerciseAdapter(List<ExerciseBlock> blocks) {
        this.blocks = blocks;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_exercise_block, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        ExerciseBlock b = blocks.get(position);

        h.detach();
        h.etExerciseName.setText(b.exercise);
        h.attach(b);

        // Meta
        int setCount = b.sets.size();
        int totalReps = 0;
        for (SetRow sr : b.sets) {
            try {
                if (sr.reps != null && !sr.reps.trim().isEmpty()) {
                    totalReps += Integer.parseInt(sr.reps.trim());
                }
            } catch (NumberFormatException ignored) {
            }
        }
        h.tvExerciseMeta.setText(setCount + " Set/s • " + totalReps + " Rep/s in total");

        // Nested sets list
        h.rvEntries.setRecycledViewPool(pool);
        h.rvEntries.setLayoutManager(new LinearLayoutManager(h.itemView.getContext()));
        SetAdapter setAdapter = new SetAdapter(b.sets, () -> {notifyItemChanged(h.getBindingAdapterPosition());
        });
        h.rvEntries.setAdapter(setAdapter);

        h.btnAddSet.setOnClickListener(v -> {
            b.sets.add(new SetRow());
            setAdapter.notifyItemInserted(b.sets.size() - 1);
            notifyItemChanged(h.getBindingAdapterPosition()); // Meta aktualisieren
            h.rvEntries.scrollToPosition(b.sets.size() - 1);
        });

        h.btnExerciseMore.setOnClickListener(v -> showExerciseMenu(v, h));
    }

    private void showExerciseMenu(View anchor, VH h) {
        int p = h.getBindingAdapterPosition();
        if (p == RecyclerView.NO_POSITION) return;

        PopupMenu popup = new PopupMenu(anchor.getContext(), anchor);
        popup.getMenu().add("Duplicate");
        popup.getMenu().add("Delete");

        popup.setOnMenuItemClickListener(item -> {
            String title = item.getTitle().toString();

            if ("Duplicate".equals(title)) {
                blocks.add(p + 1, new ExerciseBlock(blocks.get(p)));
                notifyItemInserted(p + 1);
                return true;
            }

            if ("Delete".equals(title)) {
                blocks.remove(p);
                notifyItemRemoved(p);
                return true;
            }

            return false;
        });

        popup.show();
    }

    @Override
    public int getItemCount() {
        return blocks.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        EditText etExerciseName;
        TextView tvExerciseMeta;
        ImageButton btnExerciseMore;
        com.google.android.material.button.MaterialButton btnAddSet;
        RecyclerView rvEntries;

        TextWatcher wExercise;

        VH(@NonNull View itemView) {
            super(itemView);
            etExerciseName = itemView.findViewById(R.id.etExerciseName);
            tvExerciseMeta = itemView.findViewById(R.id.tvExerciseMeta);
            btnExerciseMore = itemView.findViewById(R.id.btnExerciseMore);
            btnAddSet = itemView.findViewById(R.id.btnAddSet);
            rvEntries = itemView.findViewById(R.id.rvEntries);
        }

        void detach() {
            if (wExercise != null) etExerciseName.removeTextChangedListener(wExercise);
        }

        void attach(ExerciseBlock b) {
            wExercise = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int a, int c, int d) {}

                @Override
                public void onTextChanged(CharSequence s, int a, int c, int d) {
                    b.exercise = s.toString();
                }

                @Override
                public void afterTextChanged(Editable s) {}
            };
            etExerciseName.addTextChangedListener(wExercise);
        }
    }
}