package com.urbangaze.app.ui.explore;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.urbangaze.app.R;

public class PlacePreviewFragment extends Fragment {

    private static final String ARG_NAME = "name";
    private static final String ARG_LAT = "lat";
    private static final String ARG_LNG = "lng";
    private static final String ARG_DISTANCE = "distance";
    private static final String ARG_ADDRESS = "address";

    private String name, address;
    private double lat, lng, distance;

    public PlacePreviewFragment() {}

    public static PlacePreviewFragment newInstance(
            String name,
            double lat,
            double lng,
            double distance,
            String address
    ) {
        PlacePreviewFragment fragment = new PlacePreviewFragment();
        Bundle args = new Bundle();
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
        Button openMapsBtn = view.findViewById(R.id.openMapsBtn);
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

    private void closeFragment() {
        requireActivity().getSupportFragmentManager().popBackStack();
    }
}