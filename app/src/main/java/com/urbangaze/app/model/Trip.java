package com.urbangaze.app.model;

import java.util.List;

public class Trip {
    private String tripId;
    private String tripName;
    private String startDate;
    private String endDate;
    private List<Destination> destinations;

    public Trip() {}

    public Trip(String tripId, String tripName, String startDate, String endDate, List<Destination> destinations) {
        this.tripId = tripId;
        this.tripName = tripName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.destinations = destinations;
    }

    public String getTripId() { return tripId; }
    public String getTripName() { return tripName; }
    public String getStartDate() { return startDate; }
    public String getEndDate() { return endDate; }
    public List<Destination> getDestinations() { return destinations; }
}