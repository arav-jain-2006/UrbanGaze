package com.urbangaze.app.utils;

import android.util.Log;
import android.widget.Toast;

import com.urbangaze.app.model.Place;
import com.ola.maps.sdk.core.client.Platform;
import com.ola.maps.sdk.core.config.PlatformConfig;
import com.ola.maps.sdk.nearbysearch.client.NearbySearchClient;
import com.ola.maps.sdk.model.nearbysearch.request.NearbySearchRequest;
import com.ola.maps.sdk.model.nearbysearch.response.NearbySearchResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NearbyPlacesFetcher {

    private final NearbySearchClient nearbySearchClient;

    private final Map<String, String> placeTypeGroups = new HashMap<>();


    public NearbyPlacesFetcher() {
        PlatformConfig config = new PlatformConfig.Builder()
                .apiKey(Constants.OLA_API_KEY)
                .baseUrl("https://api.olamaps.io")
                .build();

        nearbySearchClient = Platform.getNearbySearchClient(config);
        setupPlaceTypeGroups();
    }

    private void setupPlaceTypeGroups() {
        placeTypeGroups.put("Food", "food,restaurant,cafe,bakery,bar");
        placeTypeGroups.put("Stay", "lodging,campground,rv_park");
        placeTypeGroups.put("Attractions", "tourist_attraction,museum,art_gallery,zoo,aquarium,amusement_park,landmark,point_of_interest");
        placeTypeGroups.put("Outdoors", "park,natural_feature,campground");
        placeTypeGroups.put("Shopping", "shopping_mall,store,clothing_store,department_store,book_store,jewelry_store,shoe_store,convenience_store,electronics_store,home_goods_store");
        placeTypeGroups.put("Entertainment", "movie_theater,bowling_alley,casino,night_club");
    }

    public interface NearbyFetchListener {
        void onFetchComplete(List<Place> places);
        void onError(Exception e);
    }


    public void fetchNearbyPlaces(double lat, double lng, String filter, NearbyFetchListener listener) {
        new Thread(() -> {
            try {
                NearbySearchRequest request =
                        new NearbySearchRequest.Builder()
                                .limit(50)
                                .location(lat + "," + lng)
                                .radius(2000)
                                .build();


                if (placeTypeGroups.get(filter) != null) {
                    request.setTypes(placeTypeGroups.get(filter));
                }
                NearbySearchResponse response = nearbySearchClient.nearbySearch(request);

                Map<String, Place> uniquePlaces = new HashMap<>();
                for (var item : response.getPredictions()) {
                    if (uniquePlaces.containsKey(item.getPlaceId())) continue;

                    double placeLat = item.getGeometry().getLocation().getLat();
                    double placeLng = item.getGeometry().getLocation().getLng();

                    Place place = new Place(
                            item.getPlaceId(),
                            item.getDescription(),
                            item.getStructuredFormatting().getMainText(),
                            placeLat,
                            placeLng,
                            item.getDistanceMeters()
                    );
                    uniquePlaces.put(item.getPlaceId(), place);
                }

                List<Place> results = new ArrayList<>(uniquePlaces.values());
                results = sortByPath(results, lat, lng);

                if (listener != null) listener.onFetchComplete(results);

            } catch (Exception e) {
                e.printStackTrace();
                if (listener != null) listener.onError(e);
            }
        }).start();
    }

    private double distance(double lat1, double lng1, double lat2, double lng2) {
        float[] results = new float[1];
        android.location.Location.distanceBetween(lat1, lng1, lat2, lng2, results);
        return results[0];
    }
    private List<Place> sortByPath(List<Place> places, double startLat, double startLng) {
        List<Place> remaining = new ArrayList<>(places);
        List<Place> ordered = new ArrayList<>();

        double currentLat = startLat;
        double currentLng = startLng;

        while (!remaining.isEmpty()) {
            Place nearest = null;
            double minDist = Double.MAX_VALUE;

            for (Place p : remaining) {
                double dist = distance(
                        currentLat, currentLng,
                        p.loc.getLatitude(), p.loc.getLongitude()
                );

                if (dist < minDist) {
                    minDist = dist;
                    nearest = p;
                }
            }

            ordered.add(nearest);
            remaining.remove(nearest);

            currentLat = nearest.loc.getLatitude();
            currentLng = nearest.loc.getLongitude();
        }

        return ordered;
    }
}