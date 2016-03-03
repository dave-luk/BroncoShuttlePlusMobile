package com.dave_cs.BroncoShuttlePlusMobile.LiveMaps;

import com.dave_cs.BroncoShuttlePlusServerUtil.Location;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by David on 2/23/2016.
 */
public interface PolyLineService {
    @GET("/BroncoShuttlePlus/polyline")
    Call<List<Location>> polyList(@Query("routeNumber") int routeID);
}
