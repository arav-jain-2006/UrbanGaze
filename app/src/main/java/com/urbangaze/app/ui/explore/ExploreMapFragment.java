package com.urbangaze.app.ui.explore;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.ola.mapsdk.view.Marker;
import com.urbangaze.app.data.AppData;
import com.urbangaze.app.model.Place;
import com.urbangaze.app.R;
import com.urbangaze.app.utils.Constants;

import com.ola.mapsdk.camera.MapControlSettings;
import com.ola.mapsdk.camera.OlaCameraPosition;
import com.ola.mapsdk.interfaces.OlaMapCallback;
import com.ola.mapsdk.model.OlaMarkerOptions;
import com.ola.mapsdk.model.SnippetPropertiesOptions;
import com.ola.mapsdk.view.OlaMap;
import com.ola.mapsdk.view.OlaMapView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ExploreMapFragment extends Fragment {
    OlaMapView mapView;
    OlaMap olaMap;
    ImageButton btnNext, btnPrev, backButton;
    TextView btnViewPlace;
    View navControls;

    private final Map<String, Integer> markerIndexMap = new HashMap<>();
    private int currentIndex = 0;
    private final Map<String, Place> markerData = new HashMap<>();
    private final Map<String, Marker> markers = new HashMap<>();
    Marker curr_marker;

    public ExploreMapFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_explore_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        initLocationAndMap();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void initViews(View view) {
        btnNext = view.findViewById(R.id.btnNext);
        btnPrev = view.findViewById(R.id.btnPrev);
        navControls = view.findViewById(R.id.navControls);
        btnViewPlace = view.findViewById(R.id.btnViewPlace);
        backButton = view.findViewById(R.id.backButton);
        mapView = view.findViewById(R.id.mapView);


        btnNext.setOnClickListener(v -> {
            if (AppData.getNearbyPlaces().isEmpty()) return;

            currentIndex = (currentIndex + 1) % AppData.getNearbyPlaces().size();
            showPlace(currentIndex);
        });

        btnPrev.setOnClickListener(v -> {
            if (AppData.getNearbyPlaces().isEmpty()) return;

            currentIndex = (currentIndex - 1 + AppData.getNearbyPlaces().size()) % AppData.getNearbyPlaces().size();
            showPlace(currentIndex);
        });

        btnViewPlace.setOnClickListener(v -> {
            openPlacePreview(AppData.getNearbyPlaces().get(currentIndex));
        });

        backButton.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStack("MAP", FragmentManager.POP_BACK_STACK_INCLUSIVE);
        });
    }
    private void initLocationAndMap() {
        MapControlSettings mapControlSettings = new MapControlSettings.Builder()
                .setRotateGesturesEnabled(true)
                .setScrollGesturesEnabled(true)
                .setZoomGesturesEnabled(true)
                .setCompassEnabled(false)
                .setTiltGesturesEnabled(true)
                .setDoubleTapGesturesEnabled(false)
                .build();

        mapView.getMap(Constants.OLA_API_KEY, new OlaMapCallback() {

            @Override
            public void onMapReady(OlaMap olaMapInstance) {
                olaMap = olaMapInstance;
                OlaCameraPosition campos = new OlaCameraPosition.Builder()
                        .setZoomLevel(16.0f)
                        .setDuration(500)
                        .setTilt(90)
                        .build();

                olaMap.updateCameraPosition(campos);
                addMarkers(AppData.getNearbyPlaces());
                setupOrderedPlaces();
            }

            @Override
            public void onMapError(String error) {
                Log.e("MAP", error);
            }
        }, mapControlSettings);
    }

    private void addMarkers(List<Place> places) {
        for (int i = 0; i < places.size(); i++) {
            OlaMarkerOptions markerOptions = new OlaMarkerOptions.Builder()
                    .setMarkerId(places.get(i).placeId)
                    .setPosition(places.get(i).loc)
                    .setIconSize(1.1f)
                    .setSnippet(places.get(i).title)
                    .setSnippetPropertiesOptions(new SnippetPropertiesOptions.Builder()
                            .setSnippetTextSize(14.0f)
                            .setInfoWindowStrokeColor("#1e7f75")
                            .setInfoWindowRadius(40.0f)
                            .build())
                    .setIconIntRes(R.drawable.location_pin)
                    .setIsIconClickable(true)
                    .setIsAnimationEnable(true)
                    .build();

            Marker m = olaMap.addMarker(markerOptions);
            m.hideInfoWindow();
            markers.put(places.get(i).placeId, m);
            markerData.put(places.get(i).placeId, places.get(i));
        }

        setupMarkerClick();
    }

    private void setupMarkerClick() {
        olaMap.setMarkerListener(markerId -> {
            Place place = markerData.get(markerId);

            if (place != null) {
                // handle click
                Integer index = markerIndexMap.get(markerId);
                if (index != null) {
                    currentIndex = index;
                    showPlace(currentIndex);
                }
            }
        });
    }
    private void openPlacePreview(Place place) {
        PlacePreviewFragment fragment =
                PlacePreviewFragment.newInstance(
                        place.placeId,
                        place.title,
                        place.loc.getLatitude(),
                        place.loc.getLongitude(),
                        place.distance,
                        place.address
                );

        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(
                        android.R.anim.slide_in_left,
                        android.R.anim.slide_out_right
                )
                .add(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    private void setupOrderedPlaces() {
        markerIndexMap.clear();
        for (int i = 0; i < AppData.getNearbyPlaces().size(); i++) {
            markerIndexMap.put(AppData.getNearbyPlaces().get(i).placeId, i);
        }
        currentIndex = 0;
        if (!AppData.getNearbyPlaces().isEmpty()) {
            showPlace(currentIndex);
        }
    }

    private void showPlace(int index) {
        if (AppData.getNearbyPlaces().isEmpty()) return;

        Place p = AppData.getNearbyPlaces().get(index);

        if (curr_marker != null) {
            curr_marker.hideInfoWindow();
            curr_marker.updateIconSize(1.1f);
            curr_marker.updateIconOffset(new Float[] {0f, 0f});
            curr_marker.updateIconIntRes(R.drawable.location_pin);
        }

        curr_marker = markers.get(p.placeId);
        curr_marker.showInfoWindow();
        curr_marker.updateIconSize(1.4f);
        curr_marker.updateIconOffset(new Float[] {0.0f, -3.0f});
        curr_marker.updateIconIntRes(R.drawable.location_pin_active);

        olaMap.moveCameraToLatLong(p.loc, 16f, 800);

//        highlightMarker(p.placeId);

        btnViewPlace.setText(p.title);
    }
}