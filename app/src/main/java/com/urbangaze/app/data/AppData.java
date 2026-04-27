package com.urbangaze.app.data;

import com.urbangaze.app.model.Destination;
import com.urbangaze.app.model.Place;
import com.ola.mapsdk.model.OlaLatLng;
import com.urbangaze.app.model.Trip;

import java.util.ArrayList;
import java.util.List;

public class AppData {
    public static String placeFilter;
    public static List<Place> nearbyPlaces = new ArrayList<>();

    public static List<Destination>  destinations = new ArrayList<>();

    public static Trip currentTrip;

    private static OlaLatLng userLocation;

    public static String getPlaceFilter() {
        return placeFilter;
    }
    public static void setPlaceFilter(String filter) {
        placeFilter = filter;
    }

    public static void setNearbyPlaces(List<Place> list) {
        nearbyPlaces = list;
    }
    public static List<Place> getNearbyPlaces() {
        return nearbyPlaces;
    }

    public static void setDestinations(List<Destination> list) {
        destinations = list;
    }
    public static List<Destination> getDestinations() {
        return destinations;
    }

    public static void setUserLocation(OlaLatLng loc) {
        userLocation = loc;
    }
    public static OlaLatLng getUserLocation() {
        return userLocation;
    }

    public static Trip getCurrentTrip() { return currentTrip; }
    public static void setCurrentTrip(Trip trip) { currentTrip = trip; }
}


