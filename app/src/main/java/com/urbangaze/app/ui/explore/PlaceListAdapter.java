package com.urbangaze.app.ui.explore;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.urbangaze.app.R;
import com.urbangaze.app.model.Place;

import java.util.List;

public class PlaceListAdapter extends RecyclerView.Adapter<PlaceListAdapter.PlaceViewHolder> {
    private Context context;
    private List<Place> places;

    public PlaceListAdapter(Context context, List<Place> places) {
        this.context = context;
        this.places = places;
    }

    public void updateList(List<Place> newList) {
        this.places = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PlaceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_place, parent, false);
        return new PlaceViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaceViewHolder holder, int position) {
        Place place = places.get(position);

        holder.title.setText(place.title);
        holder.address.setText(place.address);
        holder.distance.setText(place.distance + " m");

        holder.mapButton.setOnClickListener(v -> {
            openInGoogleMaps(place.loc.getLatitude(), place.loc.getLongitude(), place.title, place.address);
        });
    }

    @Override
    public int getItemCount() {
        return places.size();
    }

    private void openInGoogleMaps(double lat, double lng, String name, String address) {
        String uriStr = "geo:" + lat + "," + lng + "?q=" + Uri.encode(name);
        Uri uri = Uri.parse(uriStr);

        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.setPackage("com.google.android.apps.maps");

        if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(intent);
        } else {
            // fallback to browser
            Uri webUri = Uri.parse(
                    "https://www.google.com/maps/search/?api=1&query=" +
                            Uri.encode(address)
            );
            context.startActivity(new Intent(Intent.ACTION_VIEW, webUri));
        }
    }
    public static class PlaceViewHolder extends RecyclerView.ViewHolder {

        TextView title, address, distance, mapButton;

        public PlaceViewHolder(View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.txtName);
            address = itemView.findViewById(R.id.txtAddress);
            distance = itemView.findViewById(R.id.txtDistance);
            mapButton = itemView.findViewById(R.id.mapButton);
        }
    }
}