package com.dave_cs.BroncoShuttlePlusServerUtil.Routes;

import com.dave_cs.BroncoShuttlePlusServerUtil.Bus.BusInfo;
import com.dave_cs.BroncoShuttlePlusServerUtil.Stops.StopInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by David on 1/25/2016.
 */
public class RouteInfo {

    String route;
    List<BusInfo> busOnRoute;
    List<StopInfo> stopsOnRoute;

    public RouteInfo(JSONObject object){
        try {
            this.route = object.getString("routeName");
            JSONArray bus = object.getJSONArray("busOnRoute");
            if(bus !=null)
            {
                for(int i = 0; i < bus.length(); i++)
                {
                    this.busOnRoute.add(new BusInfo(bus.getJSONObject(i)));
                }
            }

            JSONArray stops = object.getJSONArray("stopsOnRoute");
            if(stops !=null)
            {
                for(int i = 0; i < stops.length(); i++)
                {
                    this.stopsOnRoute.add(new StopInfo(stops.getJSONObject(i)));
                }
            }
        }catch(JSONException e){
            e.printStackTrace();
        }
    }

    public RouteInfo() {

    }

    public List<StopInfo> getStopsOnRoute() {
        return stopsOnRoute;
    }

    public List<BusInfo> getBusOnRoute() {
        return busOnRoute;
    }

    public String getRoute() {
        return route;
    }

    @Override
    public String toString()
    {
        return route + "\t bus size: " + busOnRoute.size() + " & stop size: " + stopsOnRoute.size();
    }

}