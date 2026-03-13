package com.example.tabnotes.ui.main.archive;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tabnotes.R;
import com.example.tabnotes.data.FolderEntity;

import java.util.List;

public class SimpleFolderAdapter extends RecyclerView.Adapter<SimpleFolderAdapter.VH> {

    public interface OnClick { void onClick(FolderEntity folder); }

    private final List<FolderEntity> items;
    private final OnClick onClick;

    public SimpleFolderAdapter(List<FolderEntity> items, OnClick onClick) {
        this.items = items;
        this.onClick = onClick;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_simple_text, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        FolderEntity f = items.get(position);
        holder.tv.setText(f.name == null ? "" : f.name);
        holder.itemView.setOnClickListener(v -> onClick.onClick(f));
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tv;
        VH(@NonNull View itemView) {
            super(itemView);
            tv = itemView.findViewById(R.id.tv);
        }
    }
}
