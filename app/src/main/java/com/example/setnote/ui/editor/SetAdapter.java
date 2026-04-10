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
import androidx.recyclerview.widget.RecyclerView;

import com.example.setnote.R;

import java.util.List;

public class SetAdapter extends RecyclerView.Adapter<SetAdapter.VH> {

    private final List<SetRow> sets;
    private final OnSetChanged onChanged;
    public interface OnSetChanged {
        void onChanged();
    }
    public SetAdapter(List<SetRow> sets, OnSetChanged onChanged) {
        this.sets = sets;
        this.onChanged = onChanged;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_set_row, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        SetRow row = sets.get(position);

        h.detach();

        h.tvSetLabel.setText("Set " + (position + 1));
        h.etWeight.setText(row.weight);
        h.etReps.setText(row.reps);

        h.attach(row);

        h.btnDelSet.setOnClickListener(v -> {
            int p = h.getBindingAdapterPosition();
            if (p == RecyclerView.NO_POSITION) return;

            sets.remove(p);
            notifyItemRemoved(p);
            notifyItemRangeChanged(p, sets.size() - p);
            if (onChanged != null) onChanged.onChanged();
        });
    }

    @Override
    public int getItemCount() {
        return sets.size();
    }

    class VH extends RecyclerView.ViewHolder {
        TextView tvSetLabel;
        EditText etWeight;
        EditText etReps;
        ImageButton btnDelSet;

        TextWatcher wWeight;
        TextWatcher wReps;

        VH(@NonNull View itemView) {
            super(itemView);
            tvSetLabel = itemView.findViewById(R.id.tvSetLabel);
            etWeight = itemView.findViewById(R.id.etWeight);
            etReps = itemView.findViewById(R.id.etReps);
            btnDelSet = itemView.findViewById(R.id.btnDelSet);
        }

        void detach() {
            if (wWeight != null) etWeight.removeTextChangedListener(wWeight);
            if (wReps != null) etReps.removeTextChangedListener(wReps);
        }

        void attach(SetRow row) {
            wWeight = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    row.weight = s.toString();
                    if (onChanged != null) onChanged.onChanged();
                }

                @Override
                public void afterTextChanged(Editable s) {}
            };

            wReps = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    row.reps = s.toString();
                    if (onChanged != null) onChanged.onChanged();
                }

                @Override
                public void afterTextChanged(Editable s) {}

            };

            etWeight.addTextChangedListener(wWeight);
            etReps.addTextChangedListener(wReps);
        }
    }
}
