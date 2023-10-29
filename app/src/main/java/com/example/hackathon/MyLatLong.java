package com.example.hackathon;

import java.util.Date;

public class MyLatLong {
    public double latitude;
    public double longitude;
    public String description;
    public Date date;

    public MyLatLong() {
        // Default constructor is required by Firebase
    }

    public MyLatLong(double latitude, double longitude, String description, Date date) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.description = description;
        this.date = date;
    }
}