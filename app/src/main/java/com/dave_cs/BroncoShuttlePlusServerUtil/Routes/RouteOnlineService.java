package com.dave_cs.BroncoShuttlePlusServerUtil.Routes;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by David on 7/1/2016.
 */
public interface RouteOnlineService {
    @GET("/BroncoShuttlePlus/details/routesOnline")
    Call<String[]> getInfo(@Query("type") String t);
}
