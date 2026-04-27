package com.urbangaze.app.ui.me;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.urbangaze.app.R;
import com.urbangaze.app.model.Place;

import java.util.List;

public class SavedPlacesAdapter extends RecyclerView.Adapter<SavedPlacesAdapter.ViewHolder> {
    private Context context;
    private List<Place> list;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String userId = FirebaseAuth.getInstance().getUid();

    public SavedPlacesAdapter(Context context, List<Place> list) {
        this.list = list;
        this.context = context;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, address;
        ImageView remove;
        MaterialButton openMaps;

        public ViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.txtName);
            address = itemView.findViewById(R.id.txtAddress);
            remove = itemView.findViewById(R.id.btnRemove);
            openMaps = itemView.findViewById(R.id.btnOpenMaps);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_saved_place, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Place place = list.get(position);

        holder.name.setText(place.title);
        holder.address.setText(place.address);
        holder.openMaps.setOnClickListener(v -> openInMaps(place));
        holder.remove.setOnClickListener(v -> removePlace(place, holder.getAdapterPosition()));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }


    private void removePlace(Place place, int position) {
        if (userId == null) return;

        db.collection("users")
                .document(userId)
                .collection("savedPlaces")
                .document(place.placeId)
                .delete()
                .addOnSuccessListener(unused -> {
                    list.remove(position);
                    notifyItemRemoved(position);
                });
    }

    private void openInMaps(Place place) {
        String uriStr = "geo:" + place.loc.getLatitude() + "," + place.loc.getLongitude() + "?q=" + Uri.encode(place.title);
        Uri uri = Uri.parse(uriStr);

        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.setPackage("com.google.android.apps.maps");

        if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(intent);
        } else {
            Uri webUri = Uri.parse(
                    "https://www.google.com/maps/search/?api=1&query=" +
                            Uri.encode(place.address)
            );
            context.startActivity(new Intent(Intent.ACTION_VIEW, webUri));
        }
    }
}
