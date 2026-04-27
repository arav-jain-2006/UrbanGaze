package com.urbangaze.app.model;

public class Destination {

    private String title;
    private double latitude;
    private double longitude;
    private String imageUrl;

    public Destination() {} // Firestore needs a default constructor

    public Destination(String title, double latitude, double longitude, String imageUrl) {
        this.title = title;
        this.latitude = latitude;
        this.longitude = longitude;
        this.imageUrl = imageUrl;
    }

    public String getTitle() { return title; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public String getImageUrl() { return imageUrl; }

    public void setLocation(String title) { this.title = title; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}