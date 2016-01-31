package com.dave_cs.BroncoShuttlePlusServerUtil.Routes;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by David on 1/20/2016.
 */
public class SimpleRouteInfo implements Comparable{


    private String routeName;
    private String routeHours;
    private boolean inService;
    private int busCount;

    // Constructor to convert JSON object into a Java class instance
    public SimpleRouteInfo(JSONObject object){
        try {
            this.routeName = object.getString("routeName");
            this.routeHours = object.getString("routeHours");
            this.inService = object.getBoolean("inService");
            this.busCount = object.getInt("busCount");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public SimpleRouteInfo(){
        this.routeName = "No internet Connection!";
        this.inService = false;
    }


    public String getRouteHours() {
        return routeHours;
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
    public int compareTo(Object another) {
        if(another instanceof SimpleRouteInfo)
            return this.routeName.compareTo(((SimpleRouteInfo) another).getRouteName());
        else
            return this.hashCode() - another.hashCode();
    }
}
