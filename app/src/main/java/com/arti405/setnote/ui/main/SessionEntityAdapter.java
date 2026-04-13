package com.arti405.setnote.ui.main;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.arti405.setnote.R;
import com.arti405.setnote.data.SessionEntity;
import com.arti405.setnote.util.DateFormatHelper;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SessionEntityAdapter extends RecyclerView.Adapter<SessionEntityAdapter.VH> {

    public interface OnSessionClick {
        void onClick(SessionEntity session);
    }

    public interface OnSessionMoreClick {
        void onMoreClick(View anchor, SessionEntity session);
    }

    public interface OnSelectionChanged {
        void onSelectionChanged(Set<Long> selectedIds);
    }

    private final List<SessionEntity> items;
    private final OnSessionClick onClick;
    private final OnSessionMoreClick onMoreClick;
    private final OnSelectionChanged onSelectionChanged;

    private boolean selectionMode = false;
    private Set<Long> selectedIds = new HashSet<>();

    public SessionEntityAdapter(List<SessionEntity> items,
                                OnSessionClick onClick,
                                OnSessionMoreClick onMoreClick,
                                OnSelectionChanged onSelectionChanged) {
        this.items = items;
        this.onClick = onClick;
        this.onMoreClick = onMoreClick;
        this.onSelectionChanged = onSelectionChanged;
    }

    public void setSelectionMode(boolean enabled) {
        this.selectionMode = enabled;
        notifyDataSetChanged();
    }

    public void setSelectedIds(Set<Long> ids) {
        this.selectedIds = ids;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_session, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        SessionEntity s = items.get(position);

        String title = (s.title == null || s.title.trim().isEmpty()) ? "Session" : s.title.trim();
        h.tvTitle.setText(title);

        String date = DateFormatHelper.formatDate(h.itemView.getContext(), s.dateEpochMillis);
        h.tvDate.setText(date);

        boolean isSelected = selectionMode && selectedIds.contains(s.id);
        h.itemView.setAlpha(isSelected ? 0.55f : 1f);

        h.itemView.setOnClickListener(v -> {
            if (selectionMode) {
                toggleSelection(s.id);
            } else {
                onClick.onClick(s);
            }
        });

        h.btnMore.setVisibility(selectionMode ? View.GONE : View.VISIBLE);
        h.btnMore.setOnClickListener(v -> onMoreClick.onMoreClick(v, s));
    }

    private void toggleSelection(long sessionId) {
        if (selectedIds.contains(sessionId)) {
            selectedIds.remove(sessionId);
        } else {
            selectedIds.add(sessionId);
        }

        notifyDataSetChanged();

        if (onSelectionChanged != null) {
            onSelectionChanged.onSelectionChanged(new java.util.HashSet<>(selectedIds));
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDate;
        ImageButton btnMore;

        VH(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvSessionTitle);
            tvDate = itemView.findViewById(R.id.tvSessionDate);
            btnMore = itemView.findViewById(R.id.btnMore);
        }
    }
}