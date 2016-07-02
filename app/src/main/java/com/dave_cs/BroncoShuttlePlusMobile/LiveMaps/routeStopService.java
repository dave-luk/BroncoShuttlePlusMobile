package com.dave_cs.BroncoShuttlePlusMobile.LiveMaps;

import com.dave_cs.BroncoShuttlePlusServerUtil.Stops.StopInfo;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by David on 3/2/2016.
 */
public interface RouteStopService {
    @GET("/BroncoShuttlePlus/routeStopList")
    Call<List<StopInfo>> getrouteStops(@Query("routeNumber") Integer routeNumber);
}
