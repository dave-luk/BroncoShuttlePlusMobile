package com.dave_cs.BroncoShuttlePlusServerUtil.Stops;

import android.support.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by David on 1/27/2016.
 */
//@JsonIgnoreProperties(ignoreUnknown = true)
public class StopInfo implements Comparable{

    private String	name;
    private String	nextBusOfRoute;
    private String onRoute;
    private int		timeToNext;
    private int stopNumber;

    public StopInfo() {
        name = "testing!";
        nextBusOfRoute = "";
        onRoute = "";
        timeToNext = 0;
        stopNumber = 0;
    }

    public StopInfo(JSONObject object)
    {
        try {
            name = object.getString("name");
            nextBusOfRoute = object.getString("nextBusOfRoute");
            onRoute = object.getString("onRoute");
            timeToNext = object.getInt("timeToNext");
            stopNumber = object.getInt("stopNumber");
        } catch (JSONException ignored)
        {

        }
    }

    public String getName() {
        return name;
    }

    public String getNextBusOfRoute() {
        return nextBusOfRoute;
    }

    public int getTimeToNext() { return timeToNext; }

    public int getStopNumber() {
        return stopNumber;
    }

    public String getOnRoute() {
        return onRoute;
    }

    @Override
    public int compareTo(@NonNull Object another) {
        if(another instanceof StopInfo)
            return this.getName().compareTo(((StopInfo) another).getName());
        else
            return this.hashCode() - another.hashCode();
    }


}
