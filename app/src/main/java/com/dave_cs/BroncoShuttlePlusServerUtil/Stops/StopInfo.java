package com.dave_cs.BroncoShuttlePlusServerUtil.Stops;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by David on 1/27/2016.
 */
//@JsonIgnoreProperties(ignoreUnknown = true)
public class StopInfo {

    private String	name;
    private String	nextBusOfRoute;
    private int		timeToNext;

    public StopInfo() {
        name = "testing!";
        nextBusOfRoute = "";
        timeToNext = 0;
    }

    public StopInfo(JSONObject object)
    {
        try {
            name = object.getString("name");
            nextBusOfRoute = object.getString("nextBusOfRoute");
            timeToNext = object.getInt("timeToNext");
        }
        catch (JSONException e)
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
}
