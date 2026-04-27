package com.urbangaze.app.ui.explore;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.urbangaze.app.R;
import com.urbangaze.app.model.Place;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class PlaceListAdapter extends RecyclerView.Adapter<PlaceListAdapter.PlaceViewHolder> {
    private Context context;
    private List<Place> places;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String userId = FirebaseAuth.getInstance().getUid();
    private Set<String> savedIds = new HashSet<>();
    public PlaceListAdapter(Context context, List<Place> places) {
        this.context = context;
        this.places = places;
        loadSavedPlaces();
    }

    public void loadSavedPlaces() {
        if (userId == null) return;

        db.collection("users")
                .document(userId)
                .collection("savedPlaces")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    savedIds.clear();
                    for (DocumentSnapshot doc : querySnapshot) {
                        savedIds.add(doc.getId());
                    }
                    notifyDataSetChanged();
                });
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

        Log.d("BIND", "TEST");

        holder.title.setText(place.title);
        holder.address.setText(place.address);
        holder.distance.setText(place.distance + " m");

        boolean isSaved = savedIds.contains(place.placeId);
        holder.saveButton.setText(isSaved ? "Saved" : "Save");
        holder.saveButton.setBackgroundResource(
                isSaved ? R.drawable.bg_saved : R.drawable.bg_save
        );

        holder.mapButton.setOnClickListener(v -> {
            openInGoogleMaps(place.loc.getLatitude(), place.loc.getLongitude(), place.title, place.address);
        });

        holder.saveButton.setOnClickListener(v -> {
            if (userId == null) return;

            boolean currentlySaved = savedIds.contains(place.placeId);

            if (currentlySaved) {
                db.collection("users")
                        .document(userId)
                        .collection("savedPlaces")
                        .document(place.placeId)
                        .delete()
                        .addOnSuccessListener(unused -> {
                            savedIds.remove(place.placeId);
                            holder.saveButton.setText("Save");
                            holder.saveButton.setBackgroundResource(
                                    R.drawable.bg_save
                            );
                        });

            } else {
                double lat = place.loc.getLatitude();
                double lng = place.loc.getLongitude();

                Map<String, Object> data = new HashMap<>();
                data.put("placeId", place.placeId);
                data.put("title", place.title);
                data.put("address", place.address);
                data.put("lat", lat);
                data.put("lng", lng);

                db.collection("users")
                    .document(userId)
                    .collection("savedPlaces")
                    .document(place.placeId)
                    .set(data)
                    .addOnSuccessListener(unused -> {
                        savedIds.add(place.placeId);
                        holder.saveButton.setText("Saved");
                        holder.saveButton.setBackgroundResource(
                                R.drawable.bg_saved
                        );
                    });
            }


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

        TextView title, address, distance, mapButton, saveButton;

        public PlaceViewHolder(View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.txtName);
            address = itemView.findViewById(R.id.txtAddress);
            distance = itemView.findViewById(R.id.txtDistance);
            mapButton = itemView.findViewById(R.id.mapButton);
            saveButton = itemView.findViewById(R.id.saveButton);
        }
    }
}