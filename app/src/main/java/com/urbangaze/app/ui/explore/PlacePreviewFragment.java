package com.urbangaze.app.ui.explore;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;

import com.google.android.material.button.MaterialButton;
import com.urbangaze.app.R;


public class PlacePreviewFragment extends Fragment {

    private static final String ARG_PLACEID ="placeId";
    private static final String ARG_NAME = "name";
    private static final String ARG_LAT = "lat";
    private static final String ARG_LNG = "lng";
    private static final String ARG_DISTANCE = "distance";
    private static final String ARG_ADDRESS = "address";

    private String name, address, placeId;
    private double lat, lng, distance;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String userId = FirebaseAuth.getInstance().getUid();
    private boolean isSaved = false;

    public PlacePreviewFragment() {}

    public static PlacePreviewFragment newInstance(
            String placeId,
            String name,
            double lat,
            double lng,
            double distance,
            String address
    ) {
        PlacePreviewFragment fragment = new PlacePreviewFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PLACEID, placeId);
        args.putString(ARG_NAME, name);
        args.putDouble(ARG_LAT, lat);
        args.putDouble(ARG_LNG, lng);
        args.putDouble(ARG_DISTANCE, distance);
        args.putString(ARG_ADDRESS, address);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            placeId = getArguments().getString(ARG_PLACEID);
            name = getArguments().getString(ARG_NAME);
            lat = getArguments().getDouble(ARG_LAT);
            lng = getArguments().getDouble(ARG_LNG);
            distance = getArguments().getDouble(ARG_DISTANCE);
            address = getArguments().getString(ARG_ADDRESS);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_place_preview, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView placeName = view.findViewById(R.id.placeName);
        TextView placeAddress = view.findViewById(R.id.placeAddress);
        TextView placeDistance = view.findViewById(R.id.placeDistance);
        MaterialButton openMapsBtn = view.findViewById(R.id.openMapsBtn);
        MaterialButton saveBtn = view.findViewById(R.id.saveBtn);
        ImageButton closeBtn = view.findViewById(R.id.closeBtn);



        placeName.setText(name);

        if (address != null && !address.isEmpty()) {
            placeAddress.setText("📍 " + address);
        } else {
            placeAddress.setText("📍 Address not available");
        }

        String distanceText;
        if (distance < 1000) {
            distanceText = (int) distance + " m away";
        } else {
            distanceText = String.format("%.1f km away", distance / 1000);
        }
        placeDistance.setText(distanceText);
        openMapsBtn.setOnClickListener(v -> openInGoogleMaps());

        closeBtn.setOnClickListener(v -> closeFragment());

        checkIfSaved(saveBtn);
        saveBtn.setOnClickListener(v -> {

            if (userId == null) return;

            DocumentReference ref = db.collection("users")
                    .document(userId)
                    .collection("savedPlaces")
                    .document(placeId);

            if (isSaved) {
                ref.delete().addOnSuccessListener(unused -> {
                    isSaved = false;
                    updateSaveUI(saveBtn);
                });

            } else {
                Map<String, Object> data = new HashMap<>();
                data.put("placeId", placeId);
                data.put("title", name);
                data.put("address", address);
                data.put("lat", lat);
                data.put("lng", lng);

                ref.set(data).addOnSuccessListener(unused -> {
                    isSaved = true;
                    updateSaveUI(saveBtn);
                });
            }

            sendSavedUpdate();
        });
    }

    private void sendSavedUpdate() {
        requireActivity().getSupportFragmentManager().setFragmentResult(
                "refresh_saved_places",
                new Bundle()
        );
    }
    private void openInGoogleMaps() {
        String uriStr = "geo:" + lat + "," + lng + "?q=" + Uri.encode(name);
        Uri uri = Uri.parse(uriStr);

        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.setPackage("com.google.android.apps.maps");

        if (intent.resolveActivity(requireActivity().getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Uri webUri = Uri.parse(
                    "https://www.google.com/maps/search/?api=1&query=" +
                            Uri.encode(address)
            );
            startActivity(new Intent(Intent.ACTION_VIEW, webUri));
        }
    }

    private void checkIfSaved(MaterialButton saveBtn) {

        if (userId == null) return;

        db.collection("users")
                .document(userId)
                .collection("savedPlaces")
                .document(placeId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    isSaved = snapshot.exists();
                    updateSaveUI(saveBtn);
                });
    }

    private void updateSaveUI(MaterialButton saveBtn) {
        saveBtn.setText(isSaved ? "Saved" : "Save");
        if (getContext() == null) return;
        saveBtn.setBackgroundColor(
                isSaved ? ContextCompat.getColor(getContext(), R.color.secondary) : ContextCompat.getColor(getContext(), R.color.primary)
        );
    }

    private void closeFragment() {
        requireActivity().getSupportFragmentManager().popBackStack();
    }
}