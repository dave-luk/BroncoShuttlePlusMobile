package com.dave_cs.BroncoShuttlePlusServerUtil;

import com.google.android.gms.maps.model.LatLng;

//@JsonIgnoreProperties(ignoreUnknown = true)
public class Location {
    private double lat;
    private double lng;

    private int placeID;

    public int getPlaceID() {
        return placeID;
    }
    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }

    public LatLng parseLatLng() {
        return new LatLng(getLat(), getLng());
    }

    @Override
    public String toString() {
        return "Lat: " + lat + "\nlng: " + lng + "\n";
    }
}
