package com.dave_cs.BroncoShuttlePlusServerUtil.Bus;

import org.json.JSONException;
import org.json.JSONObject;


public class BusInfo implements Comparable
{
    private String busName;
    private String route;
    private int fullness;
    private int lastUpdate;
    private String nextStop;
    private int busNumber;

    public BusInfo(JSONObject obj)
    {
        try
        {
            this.busName = obj.getString("busName");
            this.fullness = obj.getInt("fullness");
            this.route = obj.getString("route");
            this.lastUpdate = obj.getInt("lastUpdate");
            this.nextStop = obj.getString("nextStop");
            this.busNumber = obj.getInt("busNumber");
        }
        catch(JSONException e)
        {
            e.printStackTrace();
        }
    }

    public BusInfo()
    {
        this.busName = "Unavailable!";
        this.route = "";
        this.fullness = 0;
        this.lastUpdate = 0;
        this.nextStop = "";
        this.busNumber = 0;
    }


    public String getBusName() {
        return busName;
    }

    public int getFullness() {
        return fullness;
    }

    public String getNextStop() {
        return nextStop;
    }

    public int getLastUpdate() {
        return lastUpdate;
    }

    public int getBusNumber() {
        return busNumber;
    }

    public String getRoute() {
        return route;
    }

    @Override
    public int compareTo(Object another) {
        if(another instanceof BusInfo)
            return this.busName.compareTo(((BusInfo) another).getBusName());
        else
            return this.hashCode() - another.hashCode();
    }


}

