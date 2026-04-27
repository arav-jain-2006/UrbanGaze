package com.urbangaze.app.ui.trips;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.urbangaze.app.R;
import com.urbangaze.app.data.AppData;
import com.urbangaze.app.model.Destination;

import java.util.List;


public class DestinationAdapter extends RecyclerView.Adapter<DestinationAdapter.ViewHolder> {

    private Context context;
    private List<Destination> list;


    public DestinationAdapter(Context context, List<Destination> list) {
        this.context = context;
        this.list = list;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvLocation;
        ImageView imgView, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            imgView = itemView.findViewById(R.id.imgView);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_destination, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Destination d = list.get(position);
        holder.tvLocation.setText(d.getTitle());

        Glide.with(context)
                .load(Uri.parse(d.getImageUrl()))
                .into(holder.imgView);

        holder.btnDelete.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            list.remove(pos);
            notifyItemRemoved(pos);
        });
    }


    @Override
    public int getItemCount() {
        return list.size();
    }
}