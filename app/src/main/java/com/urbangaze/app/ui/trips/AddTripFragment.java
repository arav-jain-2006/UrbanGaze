package com.urbangaze.app.ui.trips;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.urbangaze.app.R;
import com.urbangaze.app.model.Destination;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddTripFragment extends Fragment {
    EditText tripName;
    TextView startDate, endDate;
    RecyclerView destinationRecycler;
    private DestinationAdapter adapter;
    Button saveButton, btnAddDestination;

    String startDateText, endDateText;
    ImageButton backBtn;

    String tripId;

    private List<Destination> destinationList = new ArrayList<>();

    private FirebaseFirestore db;

    public AddTripFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_trip, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupRecycler();
        setupAddDestination();
        setupDatePickers();
        setupEdit();
    }

    private void initViews(View view) {
        tripName = view.findViewById(R.id.tripName);
        startDate = view.findViewById(R.id.startDate);
        endDate = view.findViewById(R.id.endDate);
        destinationRecycler = view.findViewById(R.id.rvDestinations);
        btnAddDestination = view.findViewById(R.id.btnAddDestination);
        saveButton = view.findViewById(R.id.btnSaveTrip);
        backBtn = view.findViewById(R.id.backBtn);


        db = FirebaseFirestore.getInstance();

        if (startDateText != null) startDate.setText(startDateText);
        if (endDateText != null) endDate.setText(endDateText);
        saveButton.setOnClickListener(v -> saveTrip());
        backBtn.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());
    }

    private void setupEdit() {
        Bundle bundle = getArguments();
        if (bundle != null) {
            String tripId = bundle.getString("tripId");
            String name = bundle.getString("tripName");
            String start = bundle.getString("startDate");
            String end = bundle.getString("endDate");

            tripName.setText(name);
            startDate.setText(start);
            endDate.setText(end);
            startDateText = start;
            endDateText = end;

            this.tripId = tripId;
            fetchDestinations(tripId);
        }
    }
    private void setupRecycler() {
        adapter = new DestinationAdapter(requireContext(), destinationList);
        destinationRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        destinationRecycler.setAdapter(adapter);
    }

    private void setupAddDestination() {
        btnAddDestination.setOnClickListener(v -> {
            openAddDestinationFragment();
        });

        setupAddDestinationListener();
    }

    private void openAddDestinationFragment() {
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(
                        android.R.anim.slide_in_left,
                        android.R.anim.slide_out_right
                )
                .add(R.id.fragment_container, new AddDestinationFragment())
                .addToBackStack(null)
                .commit();
    }

    private void setupAddDestinationListener() {
        requireActivity()
                .getSupportFragmentManager()
                .setFragmentResultListener(
                "add_destination",
                    getViewLifecycleOwner(),
                    (key, bundle) -> {

                        String title = bundle.getString("title");
                        double lat = bundle.getDouble("lat");
                        double lng = bundle.getDouble("lng");
                        String imageUrl = bundle.getString("imageUrl");

                        Destination d = new Destination(title, lat, lng, imageUrl);

                        destinationList.add(d);
                        adapter.notifyItemInserted(destinationList.size() - 1);
                    }
        );
    }

    private void setupDatePickers() {
        startDate.setOnClickListener(v -> {
            openDatePicker(0);
        });
        endDate.setOnClickListener(v -> {
            openDatePicker(1);
        });
    }

    private void openDatePicker(int index) {
        Calendar calendar = Calendar.getInstance();

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        android.app.DatePickerDialog datePickerDialog =
                new android.app.DatePickerDialog(
                    requireContext(),
                    (view, selectedYear, selectedMonth, selectedDay) -> {

                        String date = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                        if (index == 0) {
                            startDateText = date;
                            startDate.setText(date);
                        } else {
                            endDateText = date;
                            endDate.setText(date);
                        }
                    },
                    year, month, day
            );

        datePickerDialog.show();
    }

    private void saveTrip() {

        String name = tripName.getText().toString().trim();

        if (name.isEmpty()) {
            tripName.setError("Enter trip name");
            return;
        }
        if (startDateText == null || endDateText == null) {
            return;
        }
        if (destinationList.isEmpty()) {
            return;
        }

        String userId = com.google.firebase.auth.FirebaseAuth.getInstance()
                .getCurrentUser()
                .getUid();

        boolean isEdit = (this.tripId != null);

        String finalTripId = isEdit ? this.tripId :
                db.collection("users")
                        .document(userId)
                        .collection("trips")
                        .document()
                        .getId();

        Map<String, Object> tripMap = new HashMap<>();
        tripMap.put("tripName", name);
        tripMap.put("startDate", startDateText);
        tripMap.put("endDate", endDateText);

        if (!isEdit) {
            tripMap.put("createdAt", System.currentTimeMillis());

        }


        if (isEdit) {
            db.collection("users")
                    .document(userId)
                    .collection("trips")
                    .document(finalTripId)
                    .update(tripMap)
                    .addOnSuccessListener(unused -> {
                        deleteOldDestinations(userId, finalTripId, () -> {
                            saveDestinations(userId, finalTripId);
                        });
                    });

        } else {
            db.collection("users")
                    .document(userId)
                    .collection("trips")
                    .document(finalTripId)
                    .set(tripMap)
                    .addOnSuccessListener(unused -> {
                        saveDestinations(userId, finalTripId);
                    });
        }
    }

    private void saveDestinations(String userId, String tripId) {
        if (destinationList.isEmpty()) {
            finish();
            return;
        }

        final int total = destinationList.size();
        final int[] count = {0};

        int index = 0;
        Log.d("DEBUG", destinationList.size()+"");
        for (Destination d : destinationList) {
            java.util.Map<String, Object> map = new java.util.HashMap<>();
            map.put("index", index++);
            map.put("title", d.getTitle());
            map.put("location", d.getTitle());
            map.put("lat", d.getLatitude());
            map.put("lng", d.getLongitude());
            map.put("imageUrl", d.getImageUrl());

            db.collection("users")
                    .document(userId)
                    .collection("trips")
                    .document(tripId)
                    .collection("destinations")
                    .add(map)
                    .addOnSuccessListener(unused -> {
                        count[0]++;
                        if (count[0] == total) {
                            finish();
                        }
                    });
        }
    }

    private void finish() {
        requireActivity().getSupportFragmentManager().popBackStack();
    }
    private void deleteOldDestinations(String userId, String tripId, Runnable onComplete) {
        db.collection("users")
                .document(userId)
                .collection("trips")
                .document(tripId)
                .collection("destinations")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    if (queryDocumentSnapshots.isEmpty()) {
                        onComplete.run();
                        return;
                    }

                    final int total = queryDocumentSnapshots.size();
                    final int[] count = {0};

                    for (var doc : queryDocumentSnapshots) {
                        doc.getReference().delete().addOnSuccessListener(unused -> {
                            count[0]++;
                            if (count[0] == total) {
                                onComplete.run();
                            }
                        });
                    }
                });
    }

    private void fetchDestinations(String tripId) {
        String userId = com.google.firebase.auth.FirebaseAuth.getInstance()
                .getCurrentUser()
                .getUid();

        db.collection("users")
                .document(userId)
                .collection("trips")
                .document(tripId)
                .collection("destinations")
                .orderBy("index")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    destinationList.clear();

                    for (var doc : queryDocumentSnapshots) {
                        String title = doc.getString("location");
                        double lat = doc.getDouble("lat");
                        double lng = doc.getDouble("lng");
                        String imageUrl = doc.getString("imageUrl");

                        destinationList.add(new Destination(title, lat, lng, imageUrl));
                    }

                    adapter.notifyDataSetChanged();
                });
    }

}