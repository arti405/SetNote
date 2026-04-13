package com.arti405.setnote.ui.main.archive;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.arti405.setnote.R;

import java.util.List;

public class CalendarDayAdapter extends RecyclerView.Adapter<CalendarDayAdapter.VH> {

    public interface OnDayClick {
        void onDayClick(CalendarDayItem item);
    }

    private final List<CalendarDayItem> items;
    private final OnDayClick onDayClick;

    public CalendarDayAdapter(List<CalendarDayItem> items, OnDayClick onDayClick) {
        this.items = items;
        this.onDayClick = onDayClick;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_calendar_day, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        CalendarDayItem item = items.get(position);

        if (item.dayNumber == null) {
            holder.tvDay.setText("");
            holder.vMarker.setVisibility(View.INVISIBLE);
            holder.vSelectionBackground.setVisibility(View.GONE);
            holder.itemView.setOnClickListener(null);
            holder.itemView.setEnabled(false);
            return;
        }

        holder.tvDay.setText(String.valueOf(item.dayNumber));

        if (item.marked) {
            holder.vMarker.setVisibility(View.VISIBLE);
            holder.tvDay.setAlpha(1f);
        } else {
            holder.vMarker.setVisibility(View.INVISIBLE);
            holder.tvDay.setAlpha(0.65f);
        }

        holder.tvDay.setTypeface(null, android.graphics.Typeface.NORMAL);
        holder.tvDay.setBackground(null);

        if (item.selected) {
            holder.vSelectionBackground.setVisibility(View.VISIBLE);
        } else {
            holder.vSelectionBackground.setVisibility(View.GONE);
        }

        if (item.today) {
            holder.tvDay.setTypeface(null, android.graphics.Typeface.BOLD);
            // Optional: You could add a background for today here if bg_calendar_day_today exists
            // holder.tvDay.setBackgroundResource(R.drawable.bg_calendar_day_today);
        }

        holder.itemView.setEnabled(true);
        holder.itemView.setOnClickListener(v -> {
            if (onDayClick != null) onDayClick.onDayClick(item);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvDay;
        View vMarker;
        View vSelectionBackground;

        VH(@NonNull View itemView) {
            super(itemView);
            tvDay = itemView.findViewById(R.id.tvDay);
            vMarker = itemView.findViewById(R.id.vMarker);
            vSelectionBackground = itemView.findViewById(R.id.vSelectionBackground);
        }
    }
}
