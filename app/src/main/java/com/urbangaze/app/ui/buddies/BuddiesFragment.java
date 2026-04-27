package com.urbangaze.app.ui.buddies;

import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.SnapHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.urbangaze.app.R;
import com.urbangaze.app.model.MatchUser;
import com.urbangaze.app.model.Place;

public class BuddiesFragment extends Fragment {

    LinearLayout placeCard, dateCard, toggleVisibility;
    TextView placeText, dateText, planPlace, planDate;
    Button searchBtn;

    private BuddiesAdapter adapter;
    long startDate, endDate;

    Switch discoverableToggle;
    TextView updatePlan;
    RecyclerView recycler;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    String selectedPlaceName;
    double selectedPlaceLat, selectedPlaceLng;

    public BuddiesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_buddies, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        loadMyPlan();
    }

    private void initViews(View view) {
        placeCard = view.findViewById(R.id.placeCard);
        dateCard = view.findViewById(R.id.dateCard);
        placeText = view.findViewById(R.id.placeText);
        dateText = view.findViewById(R.id.dateText);
        searchBtn = view.findViewById(R.id.searchBtn);

        planPlace = view.findViewById(R.id.planPlace);
        planDate = view.findViewById(R.id.planDate);

        toggleVisibility = view.findViewById(R.id.toggleVisibility);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        recycler = view.findViewById(R.id.buddiesRecycler);
        adapter = new BuddiesAdapter();
        recycler.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false)
        );
        recycler.setAdapter(adapter);
        SnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(recycler);

        searchBtn.setOnClickListener(v -> {

            if (selectedPlaceName == null || startDate == 0) {
                Toast.makeText(getContext(), "Select place and date", Toast.LENGTH_SHORT).show();
                return;
            }
            fetchMatches();
        });

        placeCard.setOnClickListener(v -> {
            openSearchFragment();
        });

        dateCard.setOnClickListener(v -> {

            java.util.Calendar calendar = java.util.Calendar.getInstance();

            int year = calendar.get(java.util.Calendar.YEAR);
            int month = calendar.get(java.util.Calendar.MONTH);
            int day = calendar.get(java.util.Calendar.DAY_OF_MONTH);

            android.app.DatePickerDialog startDialog =
                    new android.app.DatePickerDialog(
                            getContext(),
                            (v1, y, m, d) -> {

                                startDate = getTimeInMillis(y, m, d);
                                endDate = startDate;
                                updateDateText();

                                android.app.DatePickerDialog endDialog =
                                        new android.app.DatePickerDialog(
                                                getContext(),
                                                (v2, y2, m2, d2) -> {
                                                    endDate = getTimeInMillis(y2, m2, d2);
                                                    updateDateText();
                                                },
                                                y, m, d
                                        );

                                endDialog.getDatePicker().setMinDate(startDate);
                                endDialog.show();
                            },
                            year, month, day
                    );

            startDialog.getDatePicker().setMinDate(System.currentTimeMillis());
            startDialog.show();
        });

        setupSearchListener();


        discoverableToggle = view.findViewById(R.id.discoverableToggle);
        updatePlan = view.findViewById(R.id.updatePlan);


        updatePlan.setOnClickListener(v -> {
            if (selectedPlaceName == null || startDate == 0) {
                Toast.makeText(getContext(), "Select place & date first", Toast.LENGTH_SHORT).show();
                return;
            }

            saveUserPlan();
            updatePlanCardUI();
        });

        discoverableToggle.setOnClickListener(v -> {
            if (auth.getCurrentUser() == null) return;

            if (selectedPlaceName == null || startDate == 0) {
                Toast.makeText(getContext(), "Select place & date first", Toast.LENGTH_SHORT).show();
                discoverableToggle.setChecked(false);
                return;
            }

            saveUserPlan();
            updatePlanCardUI();
        });

        addDummyData();
    }

    private long getTimeInMillis(int year, int month, int day) {
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.set(year, month, day, 0, 0, 0);
        calendar.set(java.util.Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }
    private void addDummyData() {

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        double baseLat = 17.4411;
        double baseLng = 78.3917;

        long now = System.currentTimeMillis();
        long oneDay = 24 * 60 * 60 * 1000;

        for (int i = 0; i < 15; i++) {

            Map<String, Object> data = new HashMap<>();

            // slight variation so they’re "nearby"
            double latOffset = (Math.random() - 0.5) * 0.1;  // ±0.05
            double lngOffset = (Math.random() - 0.5) * 0.1;

            long start = now + (long)(Math.random() * 3) * oneDay;
            long end = start + (long)(Math.random() * 3 + 1) * oneDay;

            data.put("userName", "User_" + i);
            data.put("placeName", "Hyderabad");
            data.put("lat", baseLat + latOffset);
            data.put("lng", baseLng + lngOffset);
            data.put("startDate", start);
            data.put("endDate", end);
            data.put("isDiscoverable", true);
            data.put("email", "example@gmail.com");
            data.put("createdAt", System.currentTimeMillis());

            db.collection("buddy_intents")
                    .document("dummy_" + i) // predictable IDs
                    .set(data);
        }
    }
    public static String formatDateRange(long start, long end) {

        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM", Locale.getDefault());

        String startStr = sdf.format(new Date(start));
        String endStr = sdf.format(new Date(end));

        if (start == end) {
            return startStr;
        } else {
            return startStr + " - " + endStr;
        }
    }

    private void updatePlanCardUI() {
        if (discoverableToggle.isChecked())
            toggleVisibility.setBackgroundColor(Color.parseColor("#E8F5E9"));
        else
            toggleVisibility.setBackgroundColor(Color.parseColor("#e8eff1"));

        planDate.setText(formatDateRange(startDate, endDate));
        planPlace.setText(selectedPlaceName);
    }
    private void loadMyPlan() {

        if (auth.getCurrentUser() == null) return;

        String uid = auth.getCurrentUser().getUid();

        db.collection("buddy_intents")
                .document(uid)
                .get()
                .addOnSuccessListener(doc -> {

                    if (doc.exists()) {
                        Boolean isDiscoverable = doc.getBoolean("isDiscoverable");
                        String place = doc.getString("placeName");
                        Long s = doc.getLong("startDate");
                        Long e = doc.getLong("endDate");

                        Double lat = doc.getDouble("lat");
                        Double lng = doc.getDouble("lng");

                        if (place != null && s != null && e != null && lat != null && lng != null) {
                            // restore variables
                            selectedPlaceName = place;
                            selectedPlaceLat = lat;
                            selectedPlaceLng = lng;
                            startDate = s;
                            endDate = e;

                            // update UI
                            discoverableToggle.setChecked(isDiscoverable != null && isDiscoverable);
                            updatePlanCardUI();
                        }

                    }

                });
    }
    private boolean isNearby(double lat1, double lng1, double lat2, double lng2) {
        float[] results = new float[1];
        Location.distanceBetween(lat1, lng1, lat2, lng2, results);
        return (results[0] < 5000); //within 5 km
    }

    private void fetchMatches() {
        if (auth.getCurrentUser() == null) return;

        String uid = auth.getCurrentUser().getUid();

        db.collection("buddy_intents")
                .get()
                .addOnSuccessListener(query -> {

                    List<MatchUser> list = new ArrayList<>();

                    for (DocumentSnapshot doc : query.getDocuments()) {

                        if (doc.getId().equals(uid)) continue;
                        Boolean isDiscoverable = doc.getBoolean("isDiscoverable");
                        if (isDiscoverable == null || !isDiscoverable) continue;

                        Double lat = doc.getDouble("lat");
                        Double lng = doc.getDouble("lng");

                        Long s = doc.getLong("startDate");
                        Long e = doc.getLong("endDate");

                        if (lat == null || lng == null || s == null || e == null) continue;

                        // location filter
                        if (!isNearby(selectedPlaceLat, selectedPlaceLng, lat, lng)) continue;

                        // date overlap
                        if (startDate <= e && endDate >= s) {

                            list.add(new MatchUser(
                                    doc.getId(),
                                    doc.getString("userName"),
                                    doc.getString("email"),
                                    doc.getString("placeName"),
                                    lat,
                                    lng,
                                    s,
                                    e
                            ));
                        }
                    }

                    adapter.submitList(list);
                });
    }

    private void updateDateText() {
        dateText.setText(formatDateRange(startDate, endDate));
    }

    private void openSearchFragment() {
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.fragment_container, new SearchBuddiesFragment())
                .addToBackStack(null)
                .commit();
    }

    public void setupSearchListener() {
        requireActivity()
            .getSupportFragmentManager()
            .setFragmentResultListener(
                    "place_selected",
                    getViewLifecycleOwner(),
                    (key, bundle) -> {
                        Place place = bundle.getParcelable("place");
                        selectedPlaceName = place.title;
                        selectedPlaceLat = place.loc.getLatitude();
                        selectedPlaceLng = place.loc.getLongitude();

                        placeText.setText(selectedPlaceName);
                    });
    }

    private void saveUserPlan() {

        String uid = auth.getCurrentUser().getUid();

        Map<String, Object> data = new HashMap<>();
        data.put("userName", auth.getCurrentUser().getDisplayName()); // temp
        data.put("placeName", selectedPlaceName);
        data.put("lat", selectedPlaceLat);
        data.put("lng", selectedPlaceLng);
        data.put("startDate", startDate);
        data.put("endDate", endDate);
        data.put("email", auth.getCurrentUser().getEmail());
        data.put("isDiscoverable", discoverableToggle.isChecked());
        data.put("createdAt", System.currentTimeMillis());

        db.collection("buddy_intents")
                .document(uid)   // one doc per user
                .set(data)
                .addOnSuccessListener(unused ->
                        Toast.makeText(getContext(), "Plan saved", Toast.LENGTH_SHORT).show()
                );
    }
}