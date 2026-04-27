package com.urbangaze.app.model;

public class MatchUser {
    public String uid;
    public String name;
    public String email;
    public String placeName;
    public double lat;
    public double lng;
    public long startDate;
    public long endDate;

    public MatchUser() {}

    public MatchUser(String uid, String name, String email, String placeName,
                     double lat, double lng,
                     long startDate, long endDate) {

        this.uid = uid;
        this.name = name;
        this.placeName = placeName;
        this.lat = lat;
        this.lng = lng;
        this.startDate = startDate;
        this.endDate = endDate;
        this.email = email;
    }
}