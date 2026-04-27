package com.urbangaze.app.ui.explore;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.urbangaze.app.model.Place;
import com.urbangaze.app.R;

import java.util.List;

public class SuggestionAdapter extends RecyclerView.Adapter<SuggestionAdapter.ViewHolder> {

    private final List<Place> list;
    private final OnItemClick listener;

    public interface OnItemClick {
        void onClick(Place item);
    }

    public SuggestionAdapter(List<Place> list, OnItemClick listener) {
        this.list = list;
        this.listener = listener;
    }

    public void updateList(List<Place> newList) {
        list.clear();
        list.addAll(newList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_suggestion, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Place item = list.get(position);
        String titleText = item.address != null ? item.address : "";
        String[] parts = titleText.split(",", 2);

        holder.title.setText(parts[0].trim());

        if (parts.length > 1) {
            holder.subtitle.setVisibility(View.VISIBLE);
            holder.subtitle.setText(parts[1].trim());
        } else {
            holder.subtitle.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> listener.onClick(item));
    }

    @Override
    public int getItemCount() { return list.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, subtitle;
        ViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            subtitle = itemView.findViewById(R.id.subtitle);
        }
    }
}
