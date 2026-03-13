package com.example.tabnotes.ui.sessions;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tabnotes.R;
import com.example.tabnotes.data.SessionEntity;

import java.util.List;

public class SessionEntityAdapter extends RecyclerView.Adapter<SessionEntityAdapter.VH> {

    public interface OnSessionClick {
        void onClick(SessionEntity session);
    }

    public interface OnSessionMoreClick {
        void onMoreClick(View anchor, SessionEntity session);
    }

    private final List<SessionEntity> items;
    private final OnSessionClick onClick;
    private final OnSessionMoreClick onMoreClick;

    public SessionEntityAdapter(List<SessionEntity> items,
                                OnSessionClick onClick,
                                OnSessionMoreClick onMoreClick) {
        this.items = items;
        this.onClick = onClick;
        this.onMoreClick = onMoreClick;
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

        String date = android.text.format.DateFormat.format("yyyy-MM-dd", s.dateEpochMillis).toString();
        h.tvDate.setText(date);

        h.itemView.setOnClickListener(v -> onClick.onClick(s));
        h.btnMore.setOnClickListener(v -> onMoreClick.onMoreClick(v, s));
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
