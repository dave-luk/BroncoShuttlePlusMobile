package com.dave_cs.BroncoShuttlePlusServerUtil.Routes;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by David on 1/27/2016.
 */
public interface RouteInfoService {
    @GET("/BroncoShuttlePlus/details/route")
    Call<RouteInfo> getInfo(@Query("routeName") String name);
}
