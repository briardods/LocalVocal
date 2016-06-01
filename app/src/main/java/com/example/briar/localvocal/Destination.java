package com.example.briar.localvocal;

/**
 * Created by Briar on 30/05/2016.
 */
public class Destination {

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    private double latitude;
    private double longitude;
    private String city;

    public Destination(double lat, double lon, String city) {
        this.latitude = lat;
        this.longitude = lon;
        this.city = city;
    }
}
