package com.example.tabnotes.ui.editor;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tabnotes.R;

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
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_exercise_block, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        ExerciseBlock b = blocks.get(position);

        h.detach();
        h.etExercise.setText(b.exercise);
        h.attach(b);

        // Nested sets list
        h.rvSets.setRecycledViewPool(pool);
        h.rvSets.setLayoutManager(new LinearLayoutManager(h.itemView.getContext()));
        SetAdapter setAdapter = new SetAdapter(b.sets);
        h.rvSets.setAdapter(setAdapter);

        h.btnAddSet.setOnClickListener(v -> {
            b.sets.add(new SetRow());
            setAdapter.notifyItemInserted(b.sets.size() - 1);
            h.rvSets.scrollToPosition(b.sets.size() - 1);
        });

        h.btnDupExercise.setOnClickListener(v -> {
            int p = h.getBindingAdapterPosition();
            if (p == RecyclerView.NO_POSITION) return;
            blocks.add(p + 1, new ExerciseBlock(blocks.get(p)));
            notifyItemInserted(p + 1);
        });

        h.btnDelExercise.setOnClickListener(v -> {
            int p = h.getBindingAdapterPosition();
            if (p == RecyclerView.NO_POSITION) return;
            blocks.remove(p);
            notifyItemRemoved(p);
        });
    }

    @Override
    public int getItemCount() {
        return blocks.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        EditText etExercise;
        Button btnAddSet, btnDupExercise, btnDelExercise;
        RecyclerView rvSets;

        TextWatcher wExercise;

        VH(@NonNull View itemView) {
            super(itemView);
            etExercise = itemView.findViewById(R.id.etExercise);
            btnAddSet = itemView.findViewById(R.id.btnAddSet);
            btnDupExercise = itemView.findViewById(R.id.btnDupExercise);
            btnDelExercise = itemView.findViewById(R.id.btnDelExercise);
            rvSets = itemView.findViewById(R.id.rvSets);
        }

        void detach() {
            if (wExercise != null) etExercise.removeTextChangedListener(wExercise);
        }

        void attach(ExerciseBlock b) {
            wExercise = new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int a, int c, int d) {}
                @Override public void onTextChanged(CharSequence s, int a, int c, int d) { b.exercise = s.toString(); }
                @Override public void afterTextChanged(Editable s) {}
            };
            etExercise.addTextChangedListener(wExercise);
        }
    }
}
