package com.dave_cs.BroncoShuttlePlusServerUtil.Routes;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by David on 1/23/2016.
 */
public interface SimpleRouteInfoService  {
    @GET("/BroncoShuttlePlus/details/simpleRoute")
    Call<SimpleRouteInfo> getInfo(@Query("routeName") int number);
}
