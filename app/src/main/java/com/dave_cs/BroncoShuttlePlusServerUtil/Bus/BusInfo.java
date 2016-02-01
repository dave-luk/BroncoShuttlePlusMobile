package com.dave_cs.BroncoShuttlePlusServerUtil.Bus;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by David on 1/27/2016.
 */

public class BusInfo implements Comparable
{
    private String bus;
    private int fullness;
    private int lastUpdate;
    private String nextStop;

    public BusInfo(JSONObject obj)
    {
        try
        {
            this.bus = obj.getString("busName");
            this.fullness = obj.getInt("fullness");
            this.lastUpdate = obj.getInt("lastUpdate");
            this.nextStop = obj.getString("nextStop");
        }
        catch(JSONException e)
        {
            e.printStackTrace();
        }
    }

    public BusInfo()
    {
        this.bus = "Unavailable!";
        this.fullness = 0;
        this.lastUpdate = 0;
        this.nextStop = "";
    }


    public String getBusName() {
        return bus;
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

    @Override
    public int compareTo(Object another) {
        if(another instanceof BusInfo)
            return this.bus.compareTo(((BusInfo) another).getBusName());
        else
            return this.hashCode() - another.hashCode();
    }


}

