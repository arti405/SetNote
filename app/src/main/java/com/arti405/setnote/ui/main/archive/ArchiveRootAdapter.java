package com.arti405.setnote.ui.main.archive;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.arti405.setnote.R;

import java.util.List;

public class ArchiveRootAdapter extends RecyclerView.Adapter<ArchiveRootAdapter.VH> {

    public interface OnClick {
        void onClick(ArchiveRootItem item);
    }

    private final List<ArchiveRootItem> items;
    private final OnClick onClick;

    public ArchiveRootAdapter(List<ArchiveRootItem> items, OnClick onClick) {
        this.items = items;
        this.onClick = onClick;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_archive_root, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        ArchiveRootItem item = items.get(position);

        holder.tvTitle.setText(item.title);
        holder.tvSubtitle.setText(item.subtitle);

        holder.itemView.setOnClickListener(v -> onClick.onClick(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvTitle, tvSubtitle;
        ImageView ivArrow;

        VH(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvSubtitle = itemView.findViewById(R.id.tvSubtitle);
            ivArrow = itemView.findViewById(R.id.ivArrow);
        }
    }
}