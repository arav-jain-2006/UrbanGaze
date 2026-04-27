package com.urbangaze.app.ui.trips;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.urbangaze.app.R;
import com.urbangaze.app.model.Trip;

import java.util.List;

public class TripAdapter extends RecyclerView.Adapter<TripAdapter.ViewHolder> {

    public interface OnTripClickListener {
        void onViewTrip(int position);
        void onDeleteTrip(int position);

        void onEditTrip(int position);
    }

    private Context context;
    private List<Trip> trips;
    private OnTripClickListener listener;

    public TripAdapter(Context context, List<Trip> trips, OnTripClickListener listener) {
        this.context = context;
        this.trips = trips;
        this.listener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, tripDate;
        ImageView imageView;
        Button viewButton;
        ImageButton deleteButton, editButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            tripDate = itemView.findViewById(R.id.tripDate);
            imageView = itemView.findViewById(R.id.imageView);
            viewButton = itemView.findViewById(R.id.viewButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
            editButton = itemView.findViewById(R.id.editButton);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_trip, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Trip trip = trips.get(position);
        holder.title.setText(trip.getTripName());
        holder.tripDate.setText(trip.getStartDate() + " – " + trip.getEndDate());

        if (trip.getDestinations() != null && !trip.getDestinations().isEmpty()) {
            Glide.with(context)
                    .load(Uri.parse(trip.getDestinations().get(0).getImageUrl()))
                    .into(holder.imageView);
        } else {
            holder.imageView.setImageResource(R.drawable.bg_map);
        }

        holder.viewButton.setOnClickListener(v -> listener.onViewTrip(position));
        holder.deleteButton.setOnClickListener(v -> {
            listener.onDeleteTrip(position);
            Toast.makeText(context, "Trip deleted", Toast.LENGTH_SHORT).show();
        });

        holder.editButton.setOnClickListener(v -> {
            listener.onEditTrip(position);
        });
    }

    @Override
    public int getItemCount() {
        return trips.size();
    }
}