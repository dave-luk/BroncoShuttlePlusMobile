package com.dave_cs.BroncoShuttlePlusServerUtil.Stops;

import com.dave_cs.BroncoShuttlePlusServerUtil.Location;

/**
 * Created by David on 8/31/2016.
 */
public class StopLocation extends Location {

    private String name;
    private int stopNumber;
    private float dist;
    private float bearing;

    public StopLocation(String name, int num, Location l) {
        this.name = name;
        stopNumber = num;
        setLat(l.getLat());
        setLng(l.getLng());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getDist() {
        return dist;
    }

    public void setDist(float dist) {
        this.dist = dist;
    }

    public float getBearing() {
        return bearing;
    }

    public void setBearing(float bearing) {
        this.bearing = bearing;
    }

    public int getStopNumber() {
        return stopNumber;
    }

    public void setStopNumber(int stopNumber) {
        this.stopNumber = stopNumber;
    }
}
