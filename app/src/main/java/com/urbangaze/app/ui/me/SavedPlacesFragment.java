package com.urbangaze.app.ui.me;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.urbangaze.app.R;
import com.urbangaze.app.model.Place;

import java.util.ArrayList;
import java.util.List;

public class SavedPlacesFragment extends Fragment {
    ImageButton backBtn;
    LinearLayout emptyState;
    RecyclerView savedRecyclerView;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String userId = FirebaseAuth.getInstance().getUid();
    private List<Place> savedList = new ArrayList<>();
    private SavedPlacesAdapter adapter;

    public SavedPlacesFragment() {
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
        return inflater.inflate(R.layout.fragment_saved_places, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupRecycler();
    }

    private void initViews(View view) {
        backBtn = view.findViewById(R.id.backButton);
        emptyState = view.findViewById(R.id.emptyState);
        savedRecyclerView = view.findViewById(R.id.savedRecyclerView);

        backBtn.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStack();
        });

    }

    private void setupRecycler() {
        savedRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new SavedPlacesAdapter(getContext(), savedList);
        savedRecyclerView.setAdapter(adapter);

        loadSavedPlaces();
    }

    private void loadSavedPlaces() {
        if (userId == null) return;

        db.collection("users")
                .document(userId)
                .collection("savedPlaces")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    savedList.clear();

                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Place place = new Place(
                                doc.getString("placeId"),
                                doc.getString("address"),
                                doc.getString("title"),
                                doc.getDouble("lat"),
                                doc.getDouble("lng"),
                                -1
                        );

                        if (place != null) {
                            savedList.add(place);
                        }
                    }

                    adapter.notifyDataSetChanged();
                    updateEmptyState();
                });
    }

    private void updateEmptyState() {
        if (savedList.isEmpty()) {
            emptyState.setVisibility(View.VISIBLE);
        } else {
            emptyState.setVisibility(View.GONE);
        }
    }

}