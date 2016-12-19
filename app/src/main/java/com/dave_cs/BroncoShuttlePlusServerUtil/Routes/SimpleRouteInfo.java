package com.dave_cs.BroncoShuttlePlusServerUtil.Routes;

import android.support.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by David on 1/20/2016.
 */
public class SimpleRouteInfo implements Comparable<SimpleRouteInfo> {


    private String routeName;
    private boolean inService;
    private int busCount;

    // Constructor to convert JSON object into a Java class instance
    public SimpleRouteInfo(JSONObject object) {
        try {
            this.routeName = object.getString("routeName");
            this.inService = object.getBoolean("inService");
            this.busCount = object.getInt("busCount");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public SimpleRouteInfo() {
        this.routeName = "No internet Connection!";
        this.inService = false;
    }

    public String getRouteName() {
        return routeName;
    }

    public boolean isInService() {
        return inService;
    }

    public int getBusCount() {
        return busCount;
    }

    @Override
    public int compareTo(@NonNull SimpleRouteInfo another) {
        return this.routeName.compareTo(another.getRouteName());
    }
}
