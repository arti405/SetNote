package com.example.setnote.ui.main.collections;

import android.view.*;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.setnote.R;
import com.example.setnote.data.CollectionEntity;
import java.util.List;

    public class SimpleCollectionAdapter extends RecyclerView.Adapter<SimpleCollectionAdapter.VH> {

        public interface OnClick {
            void onClick(CollectionEntity c);
        }

        public interface OnMoreClick {
            void onMoreClick(View anchor, CollectionEntity c);
        }

        private final List<CollectionEntity> items;
        private final OnClick onClick;
        private final OnMoreClick onMoreClick;

        public SimpleCollectionAdapter(List<CollectionEntity> items,
                                       OnClick onClick,
                                       OnMoreClick onMoreClick) {
            this.items = items;
            this.onClick = onClick;
            this.onMoreClick = onMoreClick;
        }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_simple_text, parent, false);
        return new VH(v);
    }

        @Override
        public void onBindViewHolder(@NonNull VH h, int pos) {

            CollectionEntity c = items.get(pos);

            h.tv.setText(c.name);

            h.itemView.setOnClickListener(v -> onClick.onClick(c));

            h.btnMore.setOnClickListener(v -> onMoreClick.onMoreClick(v, c));
        }

    @Override public int getItemCount() { return items.size(); }

        static class VH extends RecyclerView.ViewHolder {

            TextView tv;
            ImageButton btnMore;

            VH(View v) {
                super(v);
                tv = v.findViewById(R.id.tv);
                btnMore = v.findViewById(R.id.btnMore);
            }
        }
}