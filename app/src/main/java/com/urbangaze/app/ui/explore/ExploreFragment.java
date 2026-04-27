package com.urbangaze.app.ui.explore;


import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.ola.mapsdk.model.OlaLatLng;
import com.urbangaze.app.R;
import com.urbangaze.app.data.AppData;
import com.urbangaze.app.model.Place;
import com.urbangaze.app.utils.NearbyPlacesFetcher;

import java.util.ArrayList;
import java.util.List;

public class ExploreFragment extends Fragment {
    LinearLayout searchView;
    MaterialCardView mapView;
    ChipGroup chipGroup;
    Chip defaultChip;

    RecyclerView recyclerView;
    PlaceListAdapter adapter;

    TextView currentRegionTxt;
    double currLat, currLng;
    private FusedLocationProviderClient fusedLocationClient;
    private ActivityResultLauncher<String> locationPermissionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(),
                    isGranted -> {
                        if (isGranted) {
                            fetchLocation();
                        }
                    }
            );

    public ExploreFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_explore, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        requestLocationAtStartup();
        initViews(view);
    }
    private void requestLocationAtStartup() {
        if (ContextCompat.checkSelfPermission(requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            fetchLocation();
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }
    public void fetchLocation() {
        fusedLocationClient.getLastLocation()
            .addOnSuccessListener(requireActivity(), location -> {
                if (location != null) {
                    OlaLatLng loc = new OlaLatLng(location.getLatitude(), location.getLongitude(), 0);
                    AppData.setUserLocation(loc);
                    currLat = location.getLatitude();
                    currLng = location.getLongitude();
                    getNearbyPlaces(loc.getLatitude(), loc.getLongitude());
                }
            })
            .addOnFailureListener(e -> {
                e.printStackTrace();
            });
    }
    private void initViews(View view) {
        searchView = view.findViewById(R.id.search);
        recyclerView = view.findViewById(R.id.recyclerView);
        currentRegionTxt = view.findViewById(R.id.currentRegionTxt);
        mapView = view.findViewById(R.id.mapCard);
        chipGroup = view.findViewById(R.id.chipGroup);
        defaultChip = view.findViewById(R.id.chip_allplaces);

        setupChips();
        setupSearch();
        setupPlacesRecycler();
        setupMapView();
        setupSaveListener();
    }

    private void setupPlacesRecycler() {
        adapter = new PlaceListAdapter(requireContext(), new ArrayList<>());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    private void setupSearch() {
        searchView.setOnClickListener(v -> {
            openSearchFragment();
        });

        setupSearchListener();
    }
    private void openSearchFragment() {
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.fragment_container, new SearchFragment())
                .addToBackStack(null)
                .commit();
    }

    private void setupSearchListener() {
        requireActivity()
            .getSupportFragmentManager()
            .setFragmentResultListener(
                "place_selected",
                getViewLifecycleOwner(),
                (key, bundle) -> {
                    String title = bundle.getString("title");
                    currLat = bundle.getDouble("lat");
                    currLng = bundle.getDouble("lng");
                    currentRegionTxt.setText(title);

                    chipGroup.check(defaultChip.getId());
                    handleChipSelection(defaultChip);
                });
    }

    private void setupSaveListener() {
        requireActivity().getSupportFragmentManager()
            .setFragmentResultListener(
                "refresh_saved_places",
                getViewLifecycleOwner(),
                (key, bundle) -> {
                    if (adapter != null) {
                        adapter.loadSavedPlaces();
                    }
                }
            );
    }

    private void setupChips() {
        if (defaultChip != null) {
            chipGroup.check(defaultChip.getId());
        }

        chipGroup.setOnCheckedStateChangeListener((group, checkedId) -> {
            if (checkedId.isEmpty()) return;
            Chip selectedChip = group.findViewById(checkedId.get(0));
            if (selectedChip != null) {
                handleChipSelection(selectedChip);
            }
        });
    }

    private void handleChipSelection(Chip selectedChip) {
        String filter = selectedChip.getText().toString();
        AppData.setPlaceFilter(filter);
        getNearbyPlaces(currLat, currLng);
    }
    private void setupMapView() {
        mapView.setOnClickListener(v -> {
            if (!AppData.getNearbyPlaces().isEmpty())
                openMapFragment();
        });
    }
    private void openMapFragment() {
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.fragment_container, new ExploreMapFragment())
                .addToBackStack("MAP")
                .commit();
    }
    private void getNearbyPlaces(double lat, double lng) {
        NearbyPlacesFetcher fetcher = new NearbyPlacesFetcher();
        fetcher.fetchNearbyPlaces(lat, lng, AppData.getPlaceFilter(),
            new NearbyPlacesFetcher.NearbyFetchListener() {
                @Override
                public void onFetchComplete(List<Place> places) {
                    if (getActivity() == null) return;
                    AppData.setNearbyPlaces(places);
                    requireActivity().runOnUiThread(() -> {
                        adapter.updateList(AppData.getNearbyPlaces());
                    });
                }

                @Override
                public void onError(Exception e) {
                    // handle error
                }
            });
    }
}

