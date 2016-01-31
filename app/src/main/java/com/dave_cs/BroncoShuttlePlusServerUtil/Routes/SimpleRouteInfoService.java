package com.dave_cs.BroncoShuttlePlusServerUtil.Routes;

import com.dave_cs.BroncoShuttlePlusServerUtil.Routes.SimpleRouteInfo;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by David on 1/23/2016.
 */
public interface SimpleRouteInfoService  {
    @GET("/BroncoShuttle/details/simpleRoute")
    Call<SimpleRouteInfo> getInfo(@Query("routeName") String name);
}
