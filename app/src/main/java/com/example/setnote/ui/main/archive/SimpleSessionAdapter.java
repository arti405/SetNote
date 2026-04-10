package com.example.setnote.ui.main.archive;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.setnote.R;
import com.example.setnote.data.SessionEntity;

import java.util.List;

public class SimpleSessionAdapter extends RecyclerView.Adapter<SimpleSessionAdapter.VH> {


    public interface OnClick {
        void onClick(SessionEntity session);
    }

    public interface OnMoreClick {
        void onMoreClick(View anchor, SessionEntity session);
    }

    private final List<SessionEntity> items;
    private final OnClick onClick;
    private final OnMoreClick onMoreClick;
    private boolean selectionMode = false;
    private java.util.Set<Long> selectedIds = new java.util.HashSet<>();



    public SimpleSessionAdapter(List<SessionEntity> items,
                                OnClick onClick,
                                OnMoreClick onMoreClick) {
        this.items = items;
        this.onClick = onClick;
        this.onMoreClick = onMoreClick;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_simple_text, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {

        SessionEntity s = items.get(position);

        String title = (s.title == null || s.title.trim().isEmpty())
                ? "Session"
                : s.title.trim();

        holder.tv.setText(title);

        holder.itemView.setOnClickListener(v -> {
            if (selectionMode) {
                if (selectedIds.contains(s.id)) {
                    selectedIds.remove(s.id);
                } else {
                    selectedIds.add(s.id);
                }
                notifyItemChanged(holder.getAdapterPosition());
            } else {
                onClick.onClick(s);
            }
        });

        holder.btnMore.setOnClickListener(v -> {
            if (onMoreClick != null) {
                onMoreClick.onMoreClick(v, s);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {

        TextView tv;
        ImageButton btnMore;

        VH(@NonNull View itemView) {
            super(itemView);
            tv = itemView.findViewById(R.id.tv);
            btnMore = itemView.findViewById(R.id.btnMore);
        }
    }
}
