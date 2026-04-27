package com.urbangaze.app.ui.trips;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.urbangaze.app.R;
import com.urbangaze.app.model.Place;
import com.urbangaze.app.ui.explore.SuggestionAdapter;
import com.urbangaze.app.utils.Constants;
import com.ola.maps.sdk.core.client.Platform;
import com.ola.maps.sdk.core.config.PlatformConfig;
import com.ola.maps.sdk.model.places.request.AutocompleteRequest;
import com.ola.maps.sdk.model.places.response.AutocompleteResponse;
import com.ola.maps.sdk.places.client.PlacesClient;

import java.util.ArrayList;
import java.util.List;

public class SearchTripsFragment extends Fragment {
    EditText searchEditText;
    RecyclerView recycler;
    ImageView clearButton;
    SuggestionAdapter adapter;
    PlacesClient placesClient;
    Handler handler = new Handler(Looper.getMainLooper());
    Runnable runnable;
    List<Place> list = new ArrayList<>();

    public SearchTripsFragment() {}

    @Override
    public View onCreateView(LayoutInflater i, ViewGroup c, Bundle b) {
        return i.inflate(R.layout.fragment_search, c, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle b) {
        searchEditText = view.findViewById(R.id.searchEditText);
        recycler = view.findViewById(R.id.recycler);
        clearButton = view.findViewById(R.id.clearButton);

        setupOlaClient();
        setupSearch();
        setupRecycler();
    }

    private void setupOlaClient() {
        PlatformConfig config = new PlatformConfig.Builder()
                .apiKey(Constants.OLA_API_KEY)
                .baseUrl("https://api.olamaps.io")
                .build();

        placesClient = Platform.getPlacesClient(config);
    }

    private void setupSearch() {
        clearButton.setOnClickListener(v -> {
            searchEditText.setText("");
            list.clear();
            adapter.notifyDataSetChanged();
        });

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                clearButton.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);

                if (s.length() < 2) {
                    list.clear();
                    adapter.notifyDataSetChanged();
                    return;
                }

                handler.removeCallbacks(runnable);
                runnable = () -> fetchSuggestions(s.toString());
                handler.postDelayed(runnable, 300);
            }

            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
        });

        searchEditText.postDelayed(() -> {
            searchEditText.requestFocus();

            InputMethodManager imm = (InputMethodManager)
                    requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);

            if (imm != null) {
                imm.showSoftInput(searchEditText, InputMethodManager.SHOW_IMPLICIT);
            }
        }, 150);
    }

    private void setupRecycler() {
        adapter = new SuggestionAdapter(list, place -> {
            // handle callback
            Bundle bundle = new Bundle();
            bundle.putString("title", place.title);
            bundle.putDouble("lat", place.loc.getLatitude());
            bundle.putDouble("lng", place.loc.getLongitude());

            FragmentManager fm = requireActivity().getSupportFragmentManager();

            fm.setFragmentResult("place_selected", bundle);
            fm.popBackStack();

        });

        recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        recycler.setAdapter(adapter);
    }

    private void fetchSuggestions(String query) {
        new Thread(() -> {
            try {
                AutocompleteRequest request = new AutocompleteRequest.Builder()
                        .queryText(query)
                        .build();

                AutocompleteResponse response = placesClient.autocomplete(request);

                List<Place> results = new ArrayList<>();

                for (var item : response.getPredictions()) {
                    double placeLat = item.getGeometry().getLocation().getLat();
                    double placeLng = item.getGeometry().getLocation().getLng();

                    results.add(new Place(
                            item.getPlaceId(),
                            item.getDescription(),
                            item.getStructuredFormatting().getMainText(),
                            placeLat,
                            placeLng,
                            0
                    ));
                }

                requireActivity().runOnUiThread(() -> {
                    list.clear();
                    list.addAll(results);
                    adapter.notifyDataSetChanged();
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

}
