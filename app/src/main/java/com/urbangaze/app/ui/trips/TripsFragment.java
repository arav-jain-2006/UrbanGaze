package com.urbangaze.app.ui.trips;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.urbangaze.app.R;
import com.urbangaze.app.data.AppData;
import com.urbangaze.app.model.Destination;
import com.urbangaze.app.model.Trip;

import java.util.ArrayList;
import java.util.List;

public class TripsFragment extends Fragment {
    MaterialButton addTripButton;
    LinearLayout emptyState;
    RecyclerView recyclerView;
    TripAdapter adapter;
    List<Trip> tripList = new ArrayList<>();
    ProgressBar progressBar;
    FirebaseFirestore db;

    public TripsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_trips, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupRecycler();
        loadTrips();
    }

    private void initViews(View view) {
        addTripButton = view.findViewById(R.id.addTripButton);
        recyclerView = view.findViewById(R.id.recyclerView);
        db = FirebaseFirestore.getInstance();
        progressBar = view.findViewById(R.id.progressBar);
        emptyState = view.findViewById(R.id.emptyState);

        addTripButton.setOnClickListener(v -> {
            AppData.getDestinations().clear();
            openAddTripFragment(null);
        });
    }

    private void setupRecycler() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new TripAdapter(getContext(), tripList, new TripAdapter.OnTripClickListener() {
            @Override
            public void onViewTrip(int position) {
                Trip trip = tripList.get(position);
                if (trip == null) return;

                AppData.setCurrentTrip(trip);
                openViewTripFragment();
            }
            @Override
            public void onDeleteTrip(int position) {
                deleteTrip(tripList.get(position));
            }

            @Override
            public void onEditTrip(int position) {
                Trip trip = tripList.get(position);
                if (trip == null) return;

                editTrip(trip);
            }
        });
        recyclerView.setAdapter(adapter);
    }

    private void openViewTripFragment() {
        requireActivity().getSupportFragmentManager()
            .beginTransaction()
            .replace(R.id.fragment_container, new ViewTripFragment())
            .addToBackStack(null)
            .commit();
    }
    private void openAddTripFragment(Bundle bundle) {
        AddTripFragment fragment = new AddTripFragment();
        if (bundle != null) {
            fragment.setArguments(bundle);
        }
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    private void loadTrips() {
        progressBar.setVisibility(View.VISIBLE);
        tripList.clear();
        adapter.notifyDataSetChanged();

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("users")
                .document(userId)
                .collection("trips")
                .orderBy("createdAt")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        progressBar.setVisibility(View.GONE);
                        emptyState.setVisibility(View.VISIBLE);
                        return;
                    }

                    for (QueryDocumentSnapshot tripDoc : queryDocumentSnapshots) {
                        String tripId = tripDoc.getId();
                        String name = tripDoc.getString("tripName");
                        String start = tripDoc.getString("startDate");
                        String end = tripDoc.getString("endDate");

                        tripDoc.getReference().collection("destinations")
                                .orderBy("index")
                                .get()
                                .addOnSuccessListener(destSnapshots -> {
                                    List<Destination> destinations = new ArrayList<>();

                                    for (QueryDocumentSnapshot destDoc : destSnapshots) {
                                        String title = destDoc.getString("location");
                                        double lat = destDoc.getDouble("lat");
                                        double lng = destDoc.getDouble("lng");
                                        String imageUrl = destDoc.getString("imageUrl");
                                        destinations.add(new Destination(title, lat, lng, imageUrl));
                                    }
                                    Trip trip = new Trip(tripId, name, start, end, destinations);
                                    tripList.add(trip);
                                    adapter.notifyDataSetChanged();

                                    if (tripList.size() == queryDocumentSnapshots.size()) {
                                        progressBar.setVisibility(View.GONE);
                                        emptyState.setVisibility(View.GONE);
                                    }
                                });
                    }
                });
    }

    private void deleteTrip(Trip trip) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db.collection("users")
                .document(userId)
                .collection("trips")
                .document(trip.getTripId())
                .delete()
                .addOnSuccessListener(unused -> {
                    tripList.remove(trip);
                    adapter.notifyDataSetChanged();

                    if (tripList.isEmpty()) {
                        emptyState.setVisibility(View.VISIBLE);
                    }
                });
    }

    private void editTrip(Trip trip) {
        Bundle bundle = new Bundle();
        bundle.putString("tripId", trip.getTripId());
        bundle.putString("tripName", trip.getTripName());
        bundle.putString("startDate", trip.getStartDate());
        bundle.putString("endDate", trip.getEndDate());


        openAddTripFragment(bundle);

    }
}