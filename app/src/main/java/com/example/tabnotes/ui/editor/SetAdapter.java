package com.example.tabnotes.ui.editor;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tabnotes.R;

import java.util.List;

public class SetAdapter extends RecyclerView.Adapter<SetAdapter.VH> {

    private final List<SetRow> sets;

    public SetAdapter(List<SetRow> sets) {
        this.sets = sets;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_set_row, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        SetRow s = sets.get(position);

        h.detach();
        h.etWeight.setText(s.weight);
        h.etReps.setText(s.reps);
        h.attach(s);

        h.btnDup.setOnClickListener(v -> {
            int p = h.getBindingAdapterPosition();
            if (p == RecyclerView.NO_POSITION) return;
            sets.add(p + 1, new SetRow(sets.get(p)));
            notifyItemInserted(p + 1);
        });

        h.btnDel.setOnClickListener(v -> {
            int p = h.getBindingAdapterPosition();
            if (p == RecyclerView.NO_POSITION) return;
            if (sets.size() == 1) { // mindestens 1 Set behalten
                sets.get(0).weight = "";
                sets.get(0).reps = "";
                notifyItemChanged(0);
                return;
            }
            sets.remove(p);
            notifyItemRemoved(p);
        });
    }

    @Override
    public int getItemCount() {
        return sets.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        EditText etWeight, etReps;
        ImageButton btnDup, btnDel;
        TextWatcher wW, wR;

        VH(@NonNull View itemView) {
            super(itemView);
            etWeight = itemView.findViewById(R.id.etWeight);
            etReps = itemView.findViewById(R.id.etReps);
            btnDup = itemView.findViewById(R.id.btnDupSet);
            btnDel = itemView.findViewById(R.id.btnDelSet);
        }

        void detach() {
            if (wW != null) etWeight.removeTextChangedListener(wW);
            if (wR != null) etReps.removeTextChangedListener(wR);
        }

        void attach(SetRow s) {
            wW = simpleWatcher(v -> s.weight = v);
            wR = simpleWatcher(v -> s.reps = v);
            etWeight.addTextChangedListener(wW);
            etReps.addTextChangedListener(wR);
        }

        interface Setter { void set(String v); }

        static TextWatcher simpleWatcher(Setter setter) {
            return new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence cs, int a, int b, int c) {}
                @Override public void onTextChanged(CharSequence cs, int a, int b, int c) { setter.set(cs.toString()); }
                @Override public void afterTextChanged(Editable e) {}
            };
        }
    }
}
