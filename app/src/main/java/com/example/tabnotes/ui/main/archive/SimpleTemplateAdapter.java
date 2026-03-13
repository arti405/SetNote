package com.example.tabnotes.ui.main.archive;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tabnotes.R;
import com.example.tabnotes.data.TemplateEntity;

import java.util.List;

public class SimpleTemplateAdapter extends RecyclerView.Adapter<SimpleTemplateAdapter.VH> {

    public interface OnClick {
        void onClick(TemplateEntity t);
    }

    public interface OnMoreClick {
        void onMoreClick(View anchor, TemplateEntity t);
    }

    private final List<TemplateEntity> items;
    private final OnClick onClick;
    private final OnMoreClick onMoreClick;

    public SimpleTemplateAdapter(List<TemplateEntity> items,
                                 OnClick onClick,
                                 OnMoreClick onMoreClick) {
        this.items = items;
        this.onClick = onClick;
        this.onMoreClick = onMoreClick;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_simple_text, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        TemplateEntity t = items.get(position);
        String name = (t.name == null || t.name.trim().isEmpty()) ? "Template" : t.name.trim();

        holder.tv.setText(name);

        holder.itemView.setOnClickListener(v -> onClick.onClick(t));
        holder.btnMore.setOnClickListener(v -> {
            if (onMoreClick != null) onMoreClick.onMoreClick(v, t);
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
